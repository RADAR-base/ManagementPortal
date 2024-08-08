import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';

import {
    ProjectService,
    Principal,
    Project, OrganizationService,
    LoginService,
} from '../shared';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss',
    ],

})
export class HomeComponent {
    // implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    // projects: Project[];
    subscriptions: Subscription;
    private loginUrl = 'api/redirect/login';

    constructor(
        public principal: Principal,
        public projectService: ProjectService,
        public organizationService: OrganizationService,
        private route: ActivatedRoute,
        private loginService: LoginService,
    ) {
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.subscriptions.add(this.route.queryParams.subscribe((params) => {
            const token = params['access_token'];
            if (token) this.loginService.login(token).pipe(first()).toPromise()
        }));
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    login() {
        window.location.href = this.loginUrl
    }
}
