import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService, JhiLanguageService } from 'ng-jhipster';
import { BehaviorSubject, Observable, Subscription } from 'rxjs/Rx';
import { ITEMS_PER_PAGE, Project } from '..';

import { Source } from './source.model';
import { SourceService } from './source.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { SourceData } from '../../entities/source-data';
import { PagingParams } from '../commons';
import { EventManager } from '../util/event-manager.service';
import { parseLinks } from '../util/parse-links-util';

@Component({
    selector: 'jhi-sources',
    templateUrl: './source.component.html',
})
export class SourceComponent implements OnInit, OnDestroy, OnChanges {
    pagingParams$: Observable<PagingParams>;
    project$ = new BehaviorSubject<Project>(null);
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    @Input() isProjectSpecific: boolean;

    sources: Source[];
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

    constructor(private jhiLanguageService: JhiLanguageService,
                private sourceService: SourceService,
                private alertService: AlertService,
                private eventManager: EventManager,
                private activatedRoute: ActivatedRoute,
                private router: Router) {
        this.sources = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.map<any, PagingParams>(data => {
            const fallback = { page: 1, predicate: 'id', ascending: true };
            return data['pagingParams'] || fallback;
        });
        this.routeData = this.pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
        this.jhiLanguageService.addLocation('source');
    }

    ngOnInit() {
        this.loadSources();
        this.registerChangeInDevices();

        this.pagingParams$.subscribe(() => {
            this.loadSources();
        });
    }

    private loadSources() {
        if (this.project) {
            this.loadAllFromProject();
        } else {
            this.loadAll();
        }
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
        this.routeData.unsubscribe();
    }

    trackId(index: number, item: Source) {
        return item.sourceName;
    }

    registerChangeInDevices() {
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification',
                () => this.loadSources());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    loadAll() {
        this.sourceService.query(
                {
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    sort: this.sort(),
                },
        ).subscribe(
                (res: HttpResponse<SourceData[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    private loadAllFromProject() {
        this.sourceService.findAllByProject({
            projectName: this.project.projectName,
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort(),
        }).subscribe(
                (res: HttpResponse<Source[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
            this.loadAllFromProject();
        }
    }

    sort() {
        const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.sources = data;
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        if (!this.isProjectSpecific) {
            this.router.navigate(['/source'], {
                queryParams:
                        {
                            page: this.page,
                            sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                        },
            });
        }
        this.loadSources();
    }

}
