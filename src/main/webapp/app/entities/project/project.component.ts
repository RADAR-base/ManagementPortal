import {Component, Input} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import { map } from 'rxjs/operators';
import {Organization, Project, ProjectService} from '../../shared';

@Component({
    selector: 'jhi-projects',
    templateUrl: './project.component.html',
})
export class ProjectComponent {
    organization$ = new BehaviorSubject<Organization>(null);

    @Input()
    get organization() { return this.organization$.value; }
    set organization(v: Organization) {
        this.organization$.next(v);
    }

    projects$: Observable<Project[]>;

    constructor(
        private projectService: ProjectService,
    ) {
        this.projects$ = combineLatest([
            this.projectService.projects$,
            this.organization$,
        ]).pipe(
          map(([projects, organization]) => {
              const orgName = organization?.name;
              return orgName ? projects.filter(p => p.organization.name == orgName) : projects;
          })
        );
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }
}
