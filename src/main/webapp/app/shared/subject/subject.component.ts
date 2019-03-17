import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService, EventManager, JhiLanguageService, ParseLinks } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';
import { ITEMS_PER_PAGE, Principal, Project, ResponseWrapper } from '..';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';

@Component({
    selector: 'jhi-subjects',
    templateUrl: './subject.component.html',
})
export class SubjectComponent implements OnInit, OnDestroy, OnChanges {
    @Input() project: Project;
    subjects: Subject[];
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
    currentSearch: string;

    @Input() isProjectSpecific: boolean;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private subjectService: SubjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private principal: Principal,
            private parseLinks: ParseLinks,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.subjects = [];
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
                this.predicate = 'user.login';
                this.reverse = true;
            }
        });
        this.jhiLanguageService.addLocation('subject');
        this.currentSearch = this.activatedRoute.snapshot && this.activatedRoute.snapshot.params['search'] ?
                this.activatedRoute.snapshot.params['search'] : '';
    }

    loadSubjects() {
        if (this.isProjectSpecific) {
            this.loadAllFromProject();
        } else {
            this.loadAll();
        }
    }

    loadAll() {
        if (this.currentSearch) {
            this.subjectService.search({
                page: this.page - 1,
                query: this.currentSearch,
                size: this.itemsPerPage,
                sort: this.sort()}).subscribe(
                    (res: ResponseWrapper) => this.onSuccess(res.json, res.headers),
                    (res: ResponseWrapper) => this.onError(res.json)
            );
            return;
        }this.subjectService.query(
                {
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    sort: this.sort(),
                },
        ).subscribe(
                (res: Response) => this.onSuccess(res.json(), res.headers),
                (res: Response) => this.onError(res.json()),
        );
    }

    ngOnInit() {
        this.loadSubjects();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInSubjects();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
        this.routeData.unsubscribe();
    }

    trackLogin(index: number, item: Subject) {
        return item.login;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }

    registerChangeInSubjects() {
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', (response) => this.loadSubjects());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
            this.loadAllFromProject();
        }
    }

    private loadAllFromProject() {
        this.subjectService.findAllByProject({
            projectName: this.project.projectName,
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort(),
        }).subscribe(
                (res: Response) => {
                    this.onSuccess(res.json(), res.headers);
                },
                (res: Response) => this.onError(res.json()),
        );
    }

    sort() {
        return [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    }

    private onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.subjects = data;
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        if (!this.isProjectSpecific) {
            this.router.navigate(['/subject'], {
                queryParams:
                        {
                            page: this.page,
                            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc'),
                        },
            });
        }
        this.loadSubjects();
    }

    clear() {
        this.page = 0;
        this.currentSearch = '';
        this.router.navigate(['/subject', {
            page: this.page,
            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        }]);
        this.loadAll();
    }

    search(query) {
        if (!query) {
            return this.clear();
        }
        this.page = 0;
        this.currentSearch = query;
        this.router.navigate(['/subject', {
            search: this.currentSearch,
            page: this.page,
            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        }]);
        this.loadAll();
    }

}
