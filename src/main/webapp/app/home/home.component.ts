import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { Account, LoginModalService, Principal, Project, UserService } from '../shared';
import { EventManager } from '../shared/util/event-manager.service';
import { Observable, of, Subscription } from "rxjs";
import { switchMap, tap } from "rxjs/operators";

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss',
    ],

})
export class HomeComponent implements OnInit, OnDestroy {
    account: Account;
    modalRef: NgbModalRef;
    projects: Project[];
    subscriptions: Subscription;

    constructor(
            private principal: Principal,
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

    isAuthenticated() {
        return !!this.account;
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
