import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    OnDestroy,
} from '@angular/core';
import { combineLatest, forkJoin, Observable, Subscription, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import {
    ProjectService,
    OrganizationService,
    Project,
    Organization,
} from 'app/shared';
import { SubjectService } from 'app/shared/subject';
import { Subject as Participant } from 'app/shared/subject';
import { HttpResponse } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';

interface SubjectWithDataLogs extends Participant {
    dataLogs?: { [type: string]: string };
    _loading?: boolean;
}

interface GroupedSubjects {
    organization: Organization;
    projects: {
        project: Project;
        subjects: SubjectWithDataLogs[];
        totalItems: number;
        page: number;
    }[];
}

@Component({
    selector: 'jhi-grouped-subjects',
    templateUrl: './radar-data.component.html',
    styleUrls: ['./radar-data.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RadarDataComponent implements OnInit, OnDestroy {
    groupedSubjects$: Observable<GroupedSubjects[]>;
    groupedSubjectsData: GroupedSubjects[] | null = null;

    private subscriptions = new Subscription();

    visibleSubjectsCount: { [projectName: string]: number } = {};
    defaultVisibleCount = 10;

    visibleSubjectsMap: { [projectName: string]: SubjectWithDataLogs[] } = {};

    constructor(
        private projectService: ProjectService,
        private subjectService: SubjectService,
        private organizationService: OrganizationService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.groupedSubjects$ = this.organizationService.findAll().pipe(
            switchMap((orgs) => {
                const orgObservables = orgs.map((org) =>
                    this.projectService.findByOrganization(org.id).pipe(
                        switchMap((projects) => {
                            const projectObservables = projects.map((project) =>
                                this.subjectService
                                    .findAllByProject(
                                        project.projectName,
                                        {},
                                        { size: 1000 }
                                    )
                                    .pipe(
                                        map((res) => res.body || []),
                                        map(
                                            (
                                                subjects: SubjectWithDataLogs[]
                                            ) => ({
                                                project,
                                                subjects,
                                                totalItems: subjects.length,
                                                page: 0,
                                            })
                                        )
                                    )
                            );

                            return projectObservables.length
                                ? forkJoin(projectObservables).pipe(
                                      map((projectsWithSubjects) => {
                                          projectsWithSubjects.forEach(
                                              ({ project, subjects }) => {
                                                  this.visibleSubjectsCount[
                                                      project.projectName
                                                  ] = this.defaultVisibleCount;
                                                  this.visibleSubjectsMap[
                                                      project.projectName
                                                  ] = subjects.slice(
                                                      0,
                                                      this.defaultVisibleCount
                                                  );

                                                  this.loadDataLogsForSubjects(
                                                      project.projectName,
                                                      this.visibleSubjectsMap[
                                                          project.projectName
                                                      ]
                                                  );
                                              }
                                          );

                                          return {
                                              organization: org,
                                              projects: projectsWithSubjects,
                                          };
                                      })
                                  )
                                : of({ organization: org, projects: [] });
                        })
                    )
                );

                return combineLatest(orgObservables);
            }),
            map((data) => {
                return data;
            })
        );
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    fetchDataLogsForSubjects(
        subjects: SubjectWithDataLogs[]
    ): Observable<SubjectWithDataLogs[]> {
        if (!subjects.length) return of([]);

        return forkJoin(
            subjects.map((subject) =>
                this.subjectService.findDataLogs(subject.login).pipe(
                    map((response: HttpResponse<any>) => {
                        const logs = (response.body || []).reduce(
                            (acc, cur) => {
                                acc[cur.groupingType] = new Date(
                                    cur.time
                                ).toDateString();
                                return acc;
                            },
                            {} as { [type: string]: string }
                        );
                        subject.dataLogs = logs;
                        return subject;
                    })
                )
            )
        );
    }

    loadDataLogsForSubjects(
        projectName: string,
        subjects: SubjectWithDataLogs[]
    ): void {
        const subjectsToLoad = subjects.filter(
            (s) => !s.dataLogs && !s._loading
        );
        if (!subjectsToLoad.length) return;

        subjectsToLoad.forEach((s) => (s._loading = true));

        this.fetchDataLogsForSubjects(subjectsToLoad).subscribe(() => {
            subjectsToLoad.forEach((s) => (s._loading = false));
            this.cdr.markForCheck();
        });
    }

    showMore(
        projectName: string,
        totalSubjects: number,
        allSubjects: SubjectWithDataLogs[]
    ): void {
        const current =
            this.visibleSubjectsCount[projectName] ?? this.defaultVisibleCount;
        const next = Math.min(
            current + this.defaultVisibleCount,
            totalSubjects
        );
        this.visibleSubjectsCount[projectName] = next;

        this.visibleSubjectsMap[projectName] = allSubjects.slice(0, next);

        this.loadDataLogsForSubjects(
            projectName,
            this.visibleSubjectsMap[projectName]
        );
    }
}
