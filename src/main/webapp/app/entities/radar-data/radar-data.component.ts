import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Observable, combineLatest, forkJoin, of } from 'rxjs';
import { map, switchMap, tap } from 'rxjs/operators';
import { catchError } from 'rxjs/operators';
import {
    ProjectService,
    OrganizationService,
    Project,
    Organization,
} from 'app/shared';
import { SubjectService } from 'app/shared/subject';
import { Subject as Participant } from 'app/shared/subject';

interface GroupedSubjects {
    organization: Organization;
    projects: {
        project: Project;
        subjects: Participant[];
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
                if (!orgs.length) return of([]);
                return combineLatest(
                    orgs.map((org) =>
                        this.projectService.findByOrganization(org.id).pipe(
                            switchMap((projects) => {
                                if (!projects.length)
                                    return of({
                                        organization: org,
                                        projects: [],
                                    });
                                return combineLatest(
                                    projects.map((p) =>
                                        this.subjectService
                                            .findAllByProject(
                                                p.projectName,
                                                {},
                                                { size: 1000 }
                                            )
                                            .pipe(
                                                map((res) => ({
                                                    project: p,
                                                    subjects: res.body || [],
                                                })),
                                                catchError(() =>
                                                    of({
                                                        project: p,
                                                        subjects: [],
                                                    })
                                                )
                                            )
                                    )
                                ).pipe(
                                    map((projectsWithSubjects) => ({
                                        organization: org,
                                        projects: projectsWithSubjects,
                                    }))
                                );
                            }),
                            catchError(() =>
                                of({ organization: org, projects: [] })
                            )
                        )
                    )
                );
            }),
            catchError(() => of([]))
        );
    }
}
