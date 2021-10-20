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
import {
    SubjectService,
    SubjectFilterParams,
    SubjectsPaginationParams,
} from './subject.service';
import { PagingParams } from '../commons';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { parseLinks } from '../util/parse-links-util';

@Component({
    selector: 'jhi-subjects',
    templateUrl: './subject.component.html',
    styleUrls: ['./subject.component.scss'],
})
export class SubjectComponent implements OnInit, OnDestroy, OnChanges {
    sortingOptions = [
        'user.login',
        'externalId',
        'user.activated',
    ];
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

    filterSubjectExternalId = '';
    filterSubjectId = '';
    filterSubjectHumanReadableId = '';
    filterDateOfBirth = '';
    filterPersonName = '';
    filterEnrollmentDateFrom = '';
    filterEnrollmentDateTo = '';

    isAdvancedFilterCollapsed = true;

    checked = false;
    setOfCheckedId = new Set<number>();

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
        this.subjectService.findAllByProject(
            this.project.projectName,
            this.queryFilterParams,
            this.queryPaginationParams,
        ).subscribe(
            (res: HttpResponse<Subject[]>) => {
                this.onSuccess(res.body, res.headers);
            },
            (res: HttpErrorResponse) => this.onError(res),
        );
    }

    loadAll() {
        this.subjectService.query(
            this.queryFilterParams,
            this.queryPaginationParams,
        ).subscribe(
            (res: HttpResponse<Subject[]>) => this.onSuccess(res.body, res.headers),
            (res: HttpErrorResponse) => this.onError(res),
        );
    }

    ngOnInit() {
        if(this.isProjectSpecific){
            this.loadAllFromProject();
        } else {
            this.loadAll();
        }
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
        this.subjects = [];
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
            this.loadAllFromProject();
        }
    }

    get queryFilterParams(): SubjectFilterParams {
        const params = {
            login: this.filterSubjectId.trim() || undefined,
            externalId: this.filterSubjectExternalId.trim() || undefined,
            personName: this.filterPersonName.trim() || undefined,
            humanReadableId: this.filterSubjectHumanReadableId.trim() || undefined,
            dateOfBirth: undefined,
            enrollmentDate: undefined,
        };
        let enrollmentDateFrom = this.filterEnrollmentDateFrom.trim();
        let enrollmentDateTo = this.filterEnrollmentDateTo.trim();
        if (enrollmentDateFrom || enrollmentDateTo) {
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {
                from: enrollmentDateFrom ? enrollmentDateFrom + '[' + timeZone + ']' : undefined,
                to: enrollmentDateTo ? enrollmentDateTo + '[' + timeZone + ']' : undefined,
            };
        }
        if (this.filterDateOfBirth) {
            params.dateOfBirth = {
                is: this.filterDateOfBirth,
            };
        }

        return params;
    }

    get queryPaginationParams(): SubjectsPaginationParams {
        let subjects = this.subjects || [];
        const lastLoadedId = subjects[subjects.length - 1]?.id;
        const pageSize = this.itemsPerPage;
        return {
            lastLoadedId,
            pageSize,
            sortBy: this.predicate,
            sortDirection: this.ascending ? 'asc' : 'desc',
        };
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = +headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.subjects = [...this.subjects, ...data];
    }

    applyFilter() {
        this.subjects = [];
        this.loadSubjects();
    }

    loadMore() {
        this.page = this.page + 1;
        this.transition();
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    updateSortingSortBy(predicate) {
        this.subjects = [];
        this.predicate = predicate;
        this.page = 1;
        this.transition();
    }

    updateSortingOrder(direction) {
        this.subjects = [];
        this.ascending = direction === 'asc';
        this.page = 1;
        this.transition();
    }

    selectAll(checked: boolean = true): void {
        this.subjects.forEach(({ id }) => this.updateCheckedSet(id, checked));
        this.refreshCheckedStatus();
    }

    onItemChecked(id: number, checked: boolean): void {
        this.updateCheckedSet(id, checked);
        this.refreshCheckedStatus();
    }

    refreshCheckedStatus(): void {
        this.checked = this.subjects.every(({ id }) => this.setOfCheckedId.has(id)) && (this.subjects.length > 0);
    }

    updateCheckedSet(id: number, checked: boolean): void {
        if (checked) {
            this.setOfCheckedId.add(id);
        } else {
            this.setOfCheckedId.delete(id);
        }
    }

    addSelectedToGroup() {
        // TODO implement function
    }

    transition() {
        if (this.isProjectSpecific) {
            this.loadSubjects();
        } else {
            this.router.navigate(['/subject'], {
                queryParams: {
                    page: this.page,
                    sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                },
            }).then(() => this.loadSubjects());
        }
    }
}
