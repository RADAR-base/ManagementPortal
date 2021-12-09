import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';

import {
    Authority,
    AuthorityService,
    Organization, OrganizationService,
    Project,
    ProjectService,
    Scope
} from '../../shared';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';

import { Role } from './role.model';

@Component({
    selector: 'jhi-user-role',
    templateUrl: './role.component.html',
})
export class RoleComponent implements OnInit {
    @Input() roles: Role[];

    selectedAuthority: Authority;
    selectedProject: Project;
    selectedOrganization: Organization;

    constructor(
        public authorityService: AuthorityService,
        public projectService: ProjectService,
        public organizationService: OrganizationService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    ngOnInit() {
        if (!this.roles) {
            this.roles = [];
        }
        this.changeDetectorRef.detectChanges()
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        if (!this.selectedAuthority) {
            return;
        }
        const newRole: Role = { authorityName: this.selectedAuthority.authority };
        if (this.selectedAuthority.scope === Scope.ORGANIZATION) {
            if (!this.selectedOrganization) {
                this.alertService.error('userManagement.role.error.noOrganizationSelected', null, null);
                return;
            }
            newRole.organizationId = this.selectedOrganization.id;
            newRole.organizationName = this.selectedOrganization.name;
        }
        if (this.selectedAuthority.scope === Scope.PROJECT) {
            if (!this.selectedProject) {
                this.alertService.error('userManagement.role.error.noProjectSelected', null, null);
                return;
            }
            newRole.projectId = this.selectedProject.id;
            newRole.projectName = this.selectedProject.projectName;
        }

        if (this.hasRole(newRole)) {
            this.alertService.error('userManagement.role.error.alreadyExist', null, null);
        } else {
            this.roles.push(newRole);
        }
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    hasRole(role: Role): boolean {
        return this.roles.some(v => v.projectId === role.projectId
          && v.authorityName === role.authorityName
          && v.organizationId === role.organizationId);
    }

    removeRole(role: Role) {
        this.roles.splice(this.roles.findIndex(v => v.id === role.id), 1);
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    trackEntityById(index: number, item: any) {
        return item.id;
    }
}
