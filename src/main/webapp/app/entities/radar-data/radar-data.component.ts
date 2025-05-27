import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Observable, combineLatest, forkJoin, of } from 'rxjs';
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
    dataLogs?: { [type: string]: string }; // e.g., {"PASSIVE_ANDROID_PHONE": "2024-05-01", ...}
}

interface GroupedSubjects {
    organization: Organization;
    projects: {
        project: Project;
        subjects: SubjectWithDataLogs[];
    }[];
}

@Component({
    selector: 'jhi-grouped-subjects',
    templateUrl: './radar-data.component.html',
    styleUrls: ['./radar-data.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RadarDataComponent implements OnInit {
    groupedSubjects$: Observable<GroupedSubjects[]>;
    test$ = of('hello');

    constructor(
        private projectService: ProjectService,
        private subjectService: SubjectService,
        private organizationService: OrganizationService
    ) {}

    groupedSubjectsData: GroupedSubjects[] | null = null;

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
            })
        );
    }

    fetchDataLogsForSubjects(
        subjects: SubjectWithDataLogs[]
    ): Observable<SubjectWithDataLogs[]> {
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
}
