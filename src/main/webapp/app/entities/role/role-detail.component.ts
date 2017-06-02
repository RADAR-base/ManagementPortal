import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import {Role} from "./role.model";
import {RoleService} from "./role.service";

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
        this.jhiLanguageService.setLocations(['role', 'dataType']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInRole();
    }

    load(id) {
        this.roleService.find(id).subscribe((role) => {
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
        this.eventSubscriber = this.eventManager.subscribe('roleListModification', (response) => this.load(this.role.id));
    }
}
