import { Component, Input, OnDestroy, OnInit } from '@angular/core';

import {
    Authority,
    AuthorityService,
    Organization,
    OrganizationService,
    Project,
    ProjectService,
    Scope
} from '../../shared';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';

import { Role } from './role.model';
import { BehaviorSubject, combineLatest, Observable, Subscription } from "rxjs";
import { distinctUntilChanged, map, skip, take } from "rxjs/operators";

@Component({
    selector: 'jhi-user-role',
    templateUrl: './role.component.html',
})
export class RoleComponent implements OnInit, OnDestroy {
    roles$ = new BehaviorSubject<Role[]>([]);

    @Input()
    get roles(): Role[] { return this.roles$.value }
    set roles(newValue: Role[]) { this.roles$.next(newValue || []); }

    selectedAuthority: Authority = null;
    selectedProject: Project = null;
    selectedOrganization: Organization = null;
    allowedAuthorityNames = new Set([
        'ROLE_SYS_ADMIN',
        'ROLE_ORGANIZATION_ADMIN',
        'ROLE_PROJECT_ADMIN',
    ]);
    authorities$: Observable<Authority[]>;
    projects$: Observable<Project[]>;
    organizations$: Observable<Organization[]>;

    subscriptions = new Subscription();

    constructor(
                public authorityService: AuthorityService,
                public projectService: ProjectService,
                public organizationService: OrganizationService,
                private alertService: AlertService,
                private eventManager: EventManager
    ) {
        this.authorities$ = combineLatest([
          authorityService.authorities$,
          this.roles$,
          ]).pipe(
          map(([authorities, roles]) =>
            authorities.filter(a => this.allowedAuthorityNames.has(a.name)
              && (a.scope !== Scope.GLOBAL || !roles.some(r => r.authorityName === a.name)))
          ),
        );
        this.projects$ = combineLatest([
          projectService.projects$,
          this.roles$,
        ]).pipe(
          map(([projects, roles]) => {
              if (projects) {
                  const mappedProjects = new Set(roles
                    .filter(r => r.authorityName === 'ROLE_PROJECT_ADMIN')
                    .map(r => r.projectId)
                  )
                  return projects.filter(p => !mappedProjects.has(p.id));
              } else {
                  return [];
              }
          }),
        )
        this.organizations$ = combineLatest([
            organizationService.organizations$,
            this.roles$,
        ]).pipe(
          map(([organizations, roles]) => {
              if (organizations) {
                  const mappedOrganizations = new Set(roles
                    .filter(r => r.authorityName === 'ROLE_ORGANIZATION_ADMIN')
                    .map(r => r.organizationId)
                  );
                  return organizations.filter(o => !mappedOrganizations.has(o.id))
              } else {
                  return [];
              }
            }
          )
        );
    }

    ngOnInit() {
      this.subscriptions.add(this.registerRoleChanged());
    }

    ngOnDestroy() {
      this.subscriptions.unsubscribe();
    }

    private registerRoleChanged(): Subscription {
        return this.roles$.pipe(
          distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
          skip(1),
        ).subscribe(roles => {
          this.eventManager.broadcast({name: 'roleListModification', content: roles});
        })
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        if (!this.selectedAuthority) {
            return;
        }
        const newRole: Role = { authorityName: this.selectedAuthority.name };
        const scope = this.selectedAuthority.scope;
        if (scope === Scope.ORGANIZATION) {
            newRole.organizationId = this.selectedOrganization.id;
            newRole.organizationName = this.selectedOrganization.name;
        } else if (scope === Scope.PROJECT) {
            newRole.projectId = this.selectedProject.id;
            newRole.projectName = this.selectedProject.projectName;
        }
        const currentRoles = this.roles$.value;
        if (currentRoles.some(this.matchRole(newRole))) {
            this.alertService.error('userManagement.role.error.alreadyExist', null, null);
        } else {
            this.roles$.next([...currentRoles, newRole]);
        }
    }

    removeRole(role: Role) {
        const newRoles = [...this.roles$.value];
        newRoles.splice(newRoles.findIndex(this.matchRole(role)), 1);
        this.roles$.next(newRoles);
    }

    private matchRole(role: Role): (Role) => boolean {
        if (role.projectId) {
            return v => v.authorityName === role.authorityName
              && v.projectId === role.projectId;
        } else if (role.organizationId) {
            return v => v.authorityName === role.authorityName
              && v.organizationId === role.organizationId;
        } else {
            return v => v.authorityName === role.authorityName;
        }
    }

    trackEntityById(index: number, item: Project) {
        return item.id;
    }
}
