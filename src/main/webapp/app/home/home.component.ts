import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import {
    LoginModalService,
    Principal,
    Organization,
    UserService, OrganizationService
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
export class HomeComponent implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    organizations: Organization[];
    subscriptions: Subscription;

    constructor(
            public principal: Principal,
            private loginModalService: LoginModalService,
            private eventManager: EventManager,
            private userService: UserService,
            private organizationService: OrganizationService,
    ) {
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.loadRelevantOrganizations();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    private loadRelevantOrganizations() {
        this.subscriptions.add(this.principal.account$
            .pipe(
              switchMap(account => {
                if (account) {
                    return this.organizationService.findAll();
                    // return this.userService.findOrganization(account.login);
                } else {
                  return of([]);
                }
              })
            )
            .subscribe(organizations => this.organizations = organizations));
    }

    trackId(index: number, item: Organization) {
        return item.name;
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
