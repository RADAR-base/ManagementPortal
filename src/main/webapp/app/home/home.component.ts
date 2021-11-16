import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import {
    Account,
    LoginModalService,
    Principal,
    Project,
    ProjectService
} from '../shared';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss',
    ],

})
export class HomeComponent implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    projects: Project[];
    subscriptions: Subscription;

    constructor(
            public principal: Principal,
            private loginModalService: LoginModalService,
            private eventManager: EventManager,
            private userService: UserService,
    ) {
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.loadRelevantProjects();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    private loadRelevantProjects() {
        this.subscriptions.add(this.principal.getAuthenticationState()
            .pipe(
              tap(account => this.account = account),
              switchMap(account => {
                if (account) {
                    return this.userService.findProject(account.login);
                } else {
                  return of([]);
                }
              })
            )
            .subscribe(projects => this.projects = projects));
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
