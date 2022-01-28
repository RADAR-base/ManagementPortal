import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

import { ITEMS_PER_PAGE, Project } from '..';
import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { PagingParams } from '../commons';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { parseLinks } from '../util/parse-links-util';

@Component({
    selector: 'jhi-subjects',
    templateUrl: './subject.component.html',
})
export class SubjectComponent implements OnInit, OnDestroy, OnChanges {
    pagingParams$: Observable<PagingParams>;
    project$ = new BehaviorSubject<Project>(null);
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    subjects: Subject[];
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
            private subjectService: SubjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.subjects = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.pipe(map(data => {
            const fallback = { page: 1, predicate: 'user.login', ascending: true };
            return data['pagingParams'] || fallback;
        }));
        this.routeData = this.pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
    }

    loadSubjects() {
        if (this.isProjectSpecific) {
            this.loadAllFromProject();
        } else {
            this.loadAll();
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
        this.registerChangeInSubjects();

        this.pagingParams$.subscribe(() => {
            this.loadSubjects();
        });
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

    sort() {
        return [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
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
