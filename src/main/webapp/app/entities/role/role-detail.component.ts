import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { Role } from '../../admin/user-management/role.model';
import { EventManager } from '../../shared/util/event-manager.service';

import { RoleService } from './role.service';

@Component({
    selector: 'jhi-role-detail',
    templateUrl: './role-detail.component.html',
})
export class RoleDetailComponent implements OnInit, OnDestroy {

    role: Role;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
            private eventManager: EventManager,
            private roleService: RoleService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['projectName'], params['authorityName']);
        });
        this.registerChangeInRole();
    }

    load(projectName: string, authorityName: string) {
        this.roleService.find(projectName, authorityName).subscribe((role) => {
            this.role = role;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInRole() {
        this.eventSubscriber = this.eventManager.subscribe('roleListModification', (response) => this.load(this.role.projectName, this.role.authorityName));
    }
}
