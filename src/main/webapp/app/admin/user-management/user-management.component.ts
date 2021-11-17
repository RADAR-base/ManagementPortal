import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

import { ITEMS_PER_PAGE, Principal, User, UserService } from '../../shared';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { parseLinks } from '../../shared/util/parse-links-util';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-user-mgmt',
    templateUrl: './user-management.component.html',
})
export class UserMgmtComponent implements OnInit, OnDestroy {
    users: User[];
    error: any;
    success: any;
    subscriptions: Subscription = new Subscription();
    links: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;
    byProject: string;
    byAuthority: string;
    byLogin: string;
    byEmail: string;

    constructor(
      private userService: UserService,
      private alertService: AlertService,
      public principal: Principal,
      private eventManager: EventManager,
      private activatedRoute: ActivatedRoute,
      private router: Router,
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.subscriptions.add(this.activatedRoute.data.subscribe((data) => {
            this.page = data['pagingParams'].page;
            this.previousPage = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
        }));
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInUsers();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    registerChangeInUsers() {
        this.subscriptions.add(
            this.eventManager.subscribe('userListModification', () => this.loadAll())
        );
    }

    onChange(event: any) {
        this.subscriptions.add(
            this.userService.query({
                page: this.page - 1,
                size: this.itemsPerPage,
                authority: this.byAuthority,
                email: this.byEmail,
                login: this.byLogin,
                projectName: this.byProject,
                sort: this.sort(),
            }).subscribe(
                    (res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers),
                    (res: HttpErrorResponse) => this.onError(res),
            )
        );

    }

    loadAll() {
        this.subscriptions.add(
            this.userService.query({
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort(),
            }).subscribe(
                    (res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers),
                    (res: HttpErrorResponse) => this.onError(res),
            )
        );
    }

    trackIdentity(index, item: User) {
        return item.id;
    }

    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/user-management'], {
            queryParams:
                    {
                        page: this.page,
                        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc'),
                    },
        });
        this.loadAll();
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.users = data;
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
