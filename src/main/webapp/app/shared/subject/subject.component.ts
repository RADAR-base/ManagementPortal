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

import {Group, GroupService, ITEMS_PER_PAGE, Project} from '..';
import { Subject } from './subject.model';
import {
    SubjectService,
    SubjectFilterParams,
    SubjectPaginationParams, SubjectLastParams,
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
        'login',
        'externalId',
    ];
    pagingParams$: Observable<PagingParams>;
    project$ = new BehaviorSubject<Project>(null);
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    subjects: Subject[];
    groups: Group[];
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
    filterSubjectGroupId = '';

    isAdvancedFilterCollapsed = true;

    checked = false;
    setOfCheckedId = new Set<number>();

    @Input() isProjectSpecific: boolean;

    constructor(
            private subjectService: SubjectService,
            private groupService: GroupService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.subjects = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.pipe(map(data => {
            const fallback = { page: 1, predicate: 'login', ascending: true };
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
        if(!this.isProjectSpecific){
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
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', (result) => {
            const modifiedSubject = result.content;
            const subjectIndex = this.subjects.findIndex((s => s.id == modifiedSubject.id));
            if (subjectIndex < 0) {
                this.totalItems++;
                this.subjects = [modifiedSubject, ...this.subjects];
            } else {
                this.subjects[subjectIndex] = modifiedSubject;
            }
        });
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
            this.loadAllGroups();
        }
    }

    private loadAllGroups() {
        this.groupService.list(this.project.projectName).subscribe(
                (res: Group[]) => this.groups = res,
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    get queryFilterParams(): SubjectFilterParams {
        const params = {
            login: this.filterSubjectId.trim() || undefined,
            externalId: this.filterSubjectExternalId.trim() || undefined,
            personName: this.filterPersonName.trim() || undefined,
            humanReadableIdentifier: this.filterSubjectHumanReadableId.trim() || undefined,
            groupId: this.filterSubjectGroupId,
            dateOfBirth: undefined,
            enrollmentDate: undefined,
        };
        let enrollmentDateFrom = this.filterEnrollmentDateFrom.trim();
        let enrollmentDateTo = this.filterEnrollmentDateTo.trim();
        if (enrollmentDateFrom || enrollmentDateTo) {
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {
                from: enrollmentDateFrom ? enrollmentDateFrom + 'T00:00' + '[' + timeZone + ']' : undefined,
                to: enrollmentDateTo ? enrollmentDateTo + 'T23:59' + '[' + timeZone + ']' : undefined,
            };
        }
        if (this.filterDateOfBirth) {
            params.dateOfBirth = {
                is: this.filterDateOfBirth,
            };
        }

        return params;
    }

    get queryPaginationParams(): SubjectPaginationParams {
        const subjects = this.subjects || [];

        let last: SubjectLastParams | null;
        if (subjects.length > 0) {
            const lastSubject = subjects[subjects.length - 1]
            last = {
                id: lastSubject.id,
                login: lastSubject.login,
                externalId: lastSubject.externalId || '',
            }
        } else {
            last = null;
        }
        return {
            last,
            size: this.itemsPerPage,
            sort: [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')],
        };
    }

    private onSuccess(data, headers) {
        if(headers.get('link')){
            this.links = parseLinks(headers.get('link'));
        }
        this.totalItems = +headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        // remove redundant subjects from the list
        this.subjects = [...this.subjects, ...data];
        this.subjects = Array.from(new Set(this.subjects.map(a => a.id)))
            .map(id => {
                return this.subjects.find(a => a.id === id)
            })
    }

    applyFilter() {
        this.subjects = [];
        this.loadSubjects();
    }

    clearFilter() {
        this.filterSubjectExternalId = '';
        this.filterSubjectId = '';
        this.filterSubjectHumanReadableId = '';
        this.filterDateOfBirth = '';
        this.filterPersonName = '';
        this.filterEnrollmentDateFrom = '';
        this.filterEnrollmentDateTo = '';
        this.filterSubjectGroupId = '';
        this.applyFilter();
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
