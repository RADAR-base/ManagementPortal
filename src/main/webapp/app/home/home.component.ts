import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import {
    LoginModalService,
    ProjectService,
    Principal,
    Project, OrganizationService,
} from '../shared';
import { of, Subscription } from "rxjs";
import { EventManager } from "../shared/util/event-manager.service";
import { switchMap } from "rxjs/operators";

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

    constructor(
            public principal: Principal,
            private loginModalService: LoginModalService,
            public projectService: ProjectService,
            public organizationService: OrganizationService,

            // private eventManager: EventManager,
            // private userService: UserService,
    ) {
        this.subscriptions = new Subscription();
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
        window.location.href = "http://127.0.0.1:3000/login?return_to=http://127.0.0.1:8081/projects";
    }
    redirect_logout() {
        window.location.href = "http://127.0.0.1:4433//self-service/logout/api?return_to=http://127.0.0.1:8081";
    }
}
