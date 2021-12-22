import { Component, Input, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';

import { AuthorityService, Project, ProjectService } from '../../shared';
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

    selectedAuthority: any;
    selectedProject: Project;

    constructor(
                public authorityService: AuthorityService,
                public projectService: ProjectService,
                private alertService: AlertService,
                private eventManager: EventManager
    ) {
    }

    ngOnInit() {
        if (!this.roles) {
            this.roles = [];
        }
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        const newRole = {
            authorityName: this.selectedAuthority,
            projectId: this.selectedProject.id,
            projectName: this.selectedProject.projectName,
        }
        if (this.hasRole(newRole)) {
            this.alertService.error('userManagement.role.error.alreadyExist', null, null);
        } else {
            this.roles.push(newRole);
        }
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    hasRole(role: Role): boolean {
        return this.roles.some(v => v.projectId === role.projectId &&
                v.authorityName === role.authorityName);
    }

    removeRole(role: Role) {
        this.roles.splice(this.roles.findIndex(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }
}
