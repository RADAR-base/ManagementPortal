import { Component, Input, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import {AuthorityService, Organization, OrganizationService, Project, ProjectService} from '../../shared';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';

import { Role } from './role.model';

@Component({
    selector: 'jhi-user-role',
    templateUrl: './role.component.html',
})
export class RoleComponent implements OnInit {
    @Input() roles: Role[];
    eventSubscriber: Subscription;
    authorities: string[];

    selectedAuthority: any = null;
    selectedProject: Project = null;
    selectedOrganization: Organization = null;

    projects: Project[];

    constructor(
                private authorityService: AuthorityService,
                public projectService: ProjectService,
                public organizationService: OrganizationService,
                private alertService: AlertService,
                private eventManager: EventManager
    ) {
    }

    ngOnInit() {
        this.projectService.query().subscribe(
            (res) => this.projects = res.body,
            (error) => this.alertService.error(error.message, null, null)
        );
        if (!this.roles) {
            this.roles = [];
        }
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res;
        });
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        if (this.selectedAuthority === 'ROLE_ORGANIZATION_ADMIN') {
            const newRole = {
                authorityName: this.selectedAuthority,
                organizationId: this.selectedOrganization?.id,
                organizationName: this.selectedOrganization?.name,
            }
            if (this.hasRole(newRole)) {
                this.alertService.error('userManagement.role.error.alreadyExist', null, null);
            } else {
                this.roles.push(newRole);
            }
        } else {
            const newRole = {
                authorityName: this.selectedAuthority,
                projectId: this.selectedProject?.id,
                projectName: this.selectedProject?.projectName,
            }
            if (this.hasRole(newRole)) {
                this.alertService.error('userManagement.role.error.alreadyExist', null, null);
            } else {
                this.roles.push(newRole);
            }
        }
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    hasRole(role: Role): boolean {
        if (role.authorityName === 'ROLE_ORGANIZATION_ADMIN'){
            return this.roles.some(v => v.organizationId === role.organizationId &&
                v.authorityName === role.authorityName);
        }
        return this.roles.some(v => v.projectId === role.projectId &&
                v.authorityName === role.authorityName);
    }

    removeRole(role: Role) {
        if (role.authorityName === 'ROLE_ORGANIZATION_ADMIN') {
            this.roles.splice(this.roles.findIndex(v => v.organizationId === role.organizationId && v.authorityName === role.authorityName), 1);
        } else {
            this.roles.splice(this.roles.findIndex(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        }
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    trackOrganizationById(index: number, item: Organization) {
        return item.id;
    }
}
