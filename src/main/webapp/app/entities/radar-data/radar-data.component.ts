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

interface SubjectWithDataLogs extends Participant {
    dataLogs?: { [type: string]: string };
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

    readonly itemsPerPage = 20;

    constructor(
        private projectService: ProjectService,
        private subjectService: SubjectService,
        private organizationService: OrganizationService
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
                                        switchMap(
                                            (
                                                subjects: SubjectWithDataLogs[]
                                            ) => {
                                                if (!subjects.length)
                                                    return of({
                                                        project,
                                                        subjects: [],
                                                    });

                                                return this.fetchDataLogsForSubjects(
                                                    subjects
                                                ).pipe(
                                                    map((subjectsWithLogs) => ({
                                                        project,
                                                        subjects:
                                                            subjectsWithLogs,
                                                    }))
                                                );
                                            }
                                        )
                                    )
                            );

                            return projectObservables.length
                                ? forkJoin(projectObservables).pipe(
                                      map((projectsWithSubjects) => ({
                                          organization: org,
                                          projects: projectsWithSubjects,
                                      }))
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

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    fetchDataLogsForSubjects(
        subjects: SubjectWithDataLogs[]
    ): Observable<SubjectWithDataLogs[]> {
        if (!subjects.length) {
            return of([]);
        }
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

    visibleSubjectsCount: { [projectName: string]: number } = {};
    defaultVisibleCount = 10;

    getVisibleSubjects(
        projectName: string,
        subjects: SubjectWithDataLogs[]
    ): SubjectWithDataLogs[] {
        const count =
            this.visibleSubjectsCount[projectName] ?? this.defaultVisibleCount;
        return subjects.slice(0, count);
    }

    showMore(projectName: string, totalSubjects: number): void {
        const current =
            this.visibleSubjectsCount[projectName] ?? this.defaultVisibleCount;
        const next = Math.min(
            current + this.defaultVisibleCount,
            totalSubjects
        );
        this.visibleSubjectsCount[projectName] = next;
    }
}