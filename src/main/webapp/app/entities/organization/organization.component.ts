import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

import { ITEMS_PER_PAGE, Organization, OrganizationService } from '../../shared';
import { PagingParams } from '../../shared/commons';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';

@Component({
    selector: 'jhi-organization',
    templateUrl: './organization.component.html',
})
export class OrganizationComponent implements OnInit, OnDestroy {
    pagingParams$: Observable<PagingParams>;

    organizations: Organization[];
    eventSubscriber: Subscription;
    itemsPerPage: number;
    page: any;
    predicate: any;
    queryCount: any;
    ascending: any;
    totalItems: number;
    routeData: any;
    previousPage: any;

    constructor(
            private organizationService: OrganizationService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.organizations = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.pipe(map(data => {
            const fallback = { page: 1, predicate: 'id', ascending: true };
            return data['pagingParams'] || fallback;
        }));
        this.routeData = this.pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
    }

    loadAll() {
        this.organizationService.findAll().subscribe(
                (organizations) => this.organizations = organizations,
                (res: HttpErrorResponse) => this.onError(res.message),
        );
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInOrganizations();

        this.pagingParams$.subscribe(() => {
            this.loadAll();
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Organization) {
        return item.name;
    }

    registerChangeInOrganizations() {
        this.eventSubscriber = this.eventManager.subscribe('organizationListModification', () => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    sort() {
        const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/organization'], {
            queryParams:
                    {
                        page: this.page,
                        sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                    },
        });
        this.loadAll();
    }
}
