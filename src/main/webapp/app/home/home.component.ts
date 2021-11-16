import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import {
    Account,
    LoginModalService,
    Principal,
    Project,
    ProjectService,
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
    account: Account;
    modalRef: NgbModalRef;
    private subscriptions: Subscription;

    constructor(
            private principal: Principal,
            private loginModalService: LoginModalService,
            public projectService: ProjectService,
    ) {
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.subscriptions.add(
            this.principal.getAuthenticationState().subscribe((account) => this.account = account),
        );
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
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
