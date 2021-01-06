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
import { AlertService, EventManager, JhiLanguageService, ParseLinks } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';
import { ITEMS_PER_PAGE, Principal, Project } from '..';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { PagingParams } from '../commons';

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
    ascending: any;
    totalItems: number;
    routeData: any;
    previousPage: any;

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
        const pagingParams$ = this.activatedRoute.data.map<any, PagingParams>(data => {
            const fallback = { page: 1, predicate: 'user.login', ascending: true };
            return data['pagingParams'] || fallback;
        });
        this.routeData = pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
        this.jhiLanguageService.addLocation('subject');
    }

    loadSubjects() {
        if (this.isProjectSpecific) {
            this.loadAllFromProject();
        } else {
            this.loadAll();
        }
    }

    loadAll() {
        this.subjectService.query(
                {
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    sort: this.sort(),
                },
        ).subscribe(
                (res: HttpResponse<Subject[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res),
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
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', () => this.loadSubjects());
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
        this.subjectService.findAllByProject(this.project.projectName, {
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort(),
        }).subscribe(
                (res: HttpResponse<Subject[]>) => {
                    this.onSuccess(res.body, res.headers);
                },
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    sort() {
        return [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
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
                            sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                        },
            });
        }
        this.loadSubjects();
    }

}
