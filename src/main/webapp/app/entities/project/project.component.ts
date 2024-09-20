import {Component, Input} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Organization, Project, ProjectService} from '../../shared';

@Component({
    selector: 'jhi-projects',
    templateUrl: './project.component.html',
})
export class ProjectComponent {
    organization$ = new BehaviorSubject<Organization>(null);
    projects$: Observable<Project[]>;

    constructor(
        private projectService: ProjectService,
    ) {
        this.projects$ = this.organization$.pipe(
            switchMap(organization => this.projectService.findByOrganization(organization?.id))
        );
    }

    @Input()
    get organization() {
        return this.organization$.value;
    }

    set organization(v: Organization) {
        this.organization$.next(v);
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }
}
