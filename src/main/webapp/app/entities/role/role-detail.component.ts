import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import {RoleService} from "./role.service";
import {Role} from "../../admin/user-management/role.model";

@Component({
    selector: 'jhi-role-detail',
    templateUrl: './role-detail.component.html'
})
export class RoleDetailComponent implements OnInit, OnDestroy {

    role: Role;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['role']);
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
