import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import {
    LoginModalService,
    ProjectService,
    Principal,
    Project, OrganizationService,
} from '../shared';
import {Observable, of, Subscription} from "rxjs";
import { EventManager } from "../shared/util/event-manager.service";
import { switchMap } from "rxjs/operators";
import {SessionService} from "../shared/session/session.service";

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
    logoutUrl;
    baseUrl = "https://radar-k3s-test.thehyve.net/managementportal/" // TODO this should be the management portal baseurl from the backend?

    constructor(
            public principal: Principal,
            private loginModalService: LoginModalService,
            public projectService: ProjectService,
            public organizationService: OrganizationService,
            private sessionService: SessionService,
    ) {
        this.subscriptions = new Subscription();
        sessionService.logoutUrl$.subscribe(
            url => this.logoutUrl = url
        )
    }

    // ngOnInit() {
    //     this.loadRelevantProjects();
    // }
    //
    // ngOnDestroy() {
    //     this.subscriptions.unsubscribe();
    // }
    //
    // private loadRelevantProjects() {
    //     this.subscriptions.add(this.principal.account$
    //         .pipe(
    //           switchMap(account => {
    //             if (account) {
    //                 return this.userService.findProject(account.login);
    //             } else {
    //               return of([]);
    //             }
    //           })
    //         )
    //         .subscribe(projects => this.projects = projects));
    // }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    redirect() {
        window.location.href =  `https://radar-k3s-test.thehyve.net/kratos-ui/login?return_to=` + this.baseUrl;
    }

    redirect_logout() {
        window.location.href = this.logoutUrl + "&return_to=" + this.baseUrl;
    }
}
