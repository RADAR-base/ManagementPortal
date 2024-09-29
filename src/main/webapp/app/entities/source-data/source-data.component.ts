import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';

import {SourceData} from './source-data.model';
import {SourceDataService} from './source-data.service';
import {ITEMS_PER_PAGE} from '../../shared';
import {PagingParams} from '../../shared/commons';
import {AlertService} from '../../shared/util/alert.service';
import {EventManager} from '../../shared/util/event-manager.service';
import {parseLinks} from '../../shared/util/parse-links-util';

@Component({
    selector: 'jhi-source-data',
    templateUrl: './source-data.component.html'
})
export class SourceDataComponent implements OnInit, OnDestroy {
    pagingParams$: Observable<PagingParams>;

    sourceData: SourceData[];
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
        private sourceDataService: SourceDataService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private activatedRoute: ActivatedRoute,
        private router: Router
    ) {
        this.sourceData = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.pipe(map(data => {
            const fallback = {page: 1, predicate: 'id', ascending: true};
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
        this.sourceDataService.query(
            {
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort()
            }
        ).subscribe(
            (res: HttpResponse<SourceData[]>) => this.onSuccess(res.body, res.headers),
            (res: HttpErrorResponse) => this.onError(res)
        );
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInSourceData();

        this.pagingParams$.subscribe(() => {
            this.loadAll();
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
        this.routeData.unsubscribe();
    }

    trackId(index: number, item: SourceData) {
        return item.id;
    }

    registerChangeInSourceData() {
        this.eventSubscriber = this.eventManager.subscribe('sourceDataListModification', () => this.loadAll());
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
        this.router.navigate(['/source-data'], {
            queryParams:
                {
                    page: this.page,
                    sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc')
                }
        });
        this.loadAll();
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.sourceData = data;
    }
}
