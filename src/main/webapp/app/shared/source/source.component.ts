import {
    Component, OnInit, OnDestroy, Input, OnChanges, SimpleChanges,
    SimpleChange
} from '@angular/core';
import {Response} from '@angular/http';
import {Subscription} from 'rxjs/Rx';
import {EventManager, JhiLanguageService, AlertService, ParseLinks} from 'ng-jhipster';

import {Source} from './source.model';
import {SourceService} from './source.service';
import {Principal} from '../../shared';
import {Project} from "../../entities/project/project.model";
import {ActivatedRoute, Router} from "@angular/router";
import {ITEMS_PER_PAGE} from "../constants/pagination.constants";

@Component({
    selector: 'sources',
    templateUrl: './source.component.html'
})
export class SourceComponent implements OnInit, OnDestroy, OnChanges {

    @Input() project: Project;
    @Input() isProjectSpecific: boolean;

    sources: Source[];
    currentAccount: any;
    eventSubscriber: Subscription;
    itemsPerPage: number;
    links: any;
    page: any;
    predicate: any;
    queryCount: any;
    reverse: any;
    totalItems: number;
    routeData: any;
    previousPage: any;

    constructor(private jhiLanguageService: JhiLanguageService,
                private sourceService: SourceService,
                private alertService: AlertService,
                private eventManager: EventManager,
                private principal: Principal,
                private parseLinks: ParseLinks,
                private activatedRoute: ActivatedRoute,
                private router: Router) {
        this.sources = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.activatedRoute.data.subscribe((data) => {
            if (data['pagingParams']) {
                this.page = data['pagingParams'].page;
                this.previousPage = data['pagingParams'].page;
                this.reverse = data['pagingParams'].ascending;
                this.predicate = data['pagingParams'].predicate;
            } else {
                this.page = 1;
                this.previousPage = 1;
                this.predicate = 'id';
                // this.reverse = true;
            }
        });
        this.jhiLanguageService.setLocations(['source', 'project', 'projectStatus']);
    }

    ngOnInit() {
        this.loadSources();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInDevices();
    }

    private loadSources() {
        if (this.project) {
            this.loadAllFromProject();
        }
        else {
            this.loadAll();
        }
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Source) {
        return item.sourceName;
    }

    registerChangeInDevices() {
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification',
            (response) => this.loadSources());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    loadAll() {
        this.sourceService.query(
            {
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort()
            }
        ).subscribe(
            (res: Response) => this.onSuccess(res.json(), res.headers),
            (res: Response) => this.onError(res.json())
        );
    }

    private loadAllFromProject() {
        this.sourceService.findAllByProject({
            projectName: this.project.projectName,
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort()
        }).subscribe(
            (res: Response) => {
                this.onSuccess(res.json(), res.headers)
            },
            (res: Response) => this.onError(res.json())
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
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    private onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
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
                        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
                    }
            });
        }
        this.loadAll();
    }

}
