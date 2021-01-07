import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService, EventManager, JhiLanguageService, ParseLinks } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';
import { ITEMS_PER_PAGE } from '../../shared';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { PagingParams } from '../../shared/commons';

@Component({
    selector: 'jhi-source-type',
    templateUrl: './source-type.component.html',
})
export class SourceTypeComponent implements OnInit, OnDestroy {

    sourceTypes: SourceType[];
    eventSubscriber: Subscription;
    itemsPerPage: number;
    links: any;
    page: any;
    predicate: any;
    queryCount: any;
    ascending: any;
    totalItems: number;
    routeData: any;
    previousPage: any;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private sourceTypeService: SourceTypeService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private parseLinks: ParseLinks,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.sourceTypes = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        const pagingParams$ = this.activatedRoute.data.map<any, PagingParams>(data => {
            const fallback = { page: 1, predicate: 'id', ascending: true };
            return data['pagingParams'] || fallback;
        });
        this.routeData = pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
        this.jhiLanguageService.setLocations(['sourceType', 'sourceTypeScope']);
    }

    loadAll() {
        this.sourceTypeService.query(
                {
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    sort: this.sort(),
                },
        ).subscribe(
                (res: HttpResponse<SourceType[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInSourceTypes();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
        this.routeData.unsubscribe();
    }

    trackId(index: number, item: SourceType) {
        return item.id;
    }

    registerChangeInSourceTypes() {
        this.eventSubscriber = this.eventManager.subscribe('sourceTypeListModification', () => this.loadAll());
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

    private onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.sourceTypes = data;
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/source-type'], {
            queryParams:
                    {
                        page: this.page,
                        sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                    },
        });
        this.loadAll();
    }

}
