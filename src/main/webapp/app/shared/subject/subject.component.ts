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
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import {debounceTime, distinctUntilChanged, map} from 'rxjs/operators';

import {Group, GroupService, ITEMS_PER_PAGE, Project} from '..';
import {
    AddSubjectsToGroupDialogComponent
} from "./add-subjects-to-group-dialog.component";
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
import {NgbCalendar, NgbDate, NgbDateParserFormatter} from "@ng-bootstrap/ng-bootstrap";

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

    filters = {
        subjectId: '',
        externalId: '',
        humanReadableId: '',
        dateOfBirth: undefined,
        personName: '',
        enrollmentDateFrom: undefined,
        enrollmentDateTo: undefined,
        groupId: '',
    }

    appliedFilters = {
        dateOfBirth: undefined,
        enrollmentDateFrom: undefined,
        enrollmentDateTo: undefined,
        group: '',
    }

    filterTriggerUpdate$: BehaviorSubject<string> = new BehaviorSubject<string>('');

    isFilterApplied = false;

    enrollmentDateFromError = false;
    enrollmentDateToError = false;

    isAdvancedFilterCollapsed = true;

    allChecked = false;
    setOfCheckedId = new Set<number>();

    @Input() isProjectSpecific: boolean;

    constructor(
            private subjectService: SubjectService,
            private groupService: GroupService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private modalService: NgbModal,
            private activatedRoute: ActivatedRoute,
            private router: Router,
            private calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter
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

        this.filterTriggerUpdate$.pipe(
            debounceTime(300),
            distinctUntilChanged()
        ).subscribe(() => this.applyFilter());
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
                this.updateSubjects([modifiedSubject, ...this.subjects]);
            } else {
                this.subjects[subjectIndex] = modifiedSubject;
            }
        });
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    ngOnChanges(changes: SimpleChanges) {
        this.clearSubjects();
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
            login: this.filters.subjectId.trim() || undefined,
            externalId: this.filters.externalId.trim() || undefined,
            personName: this.filters.personName.trim() || undefined,
            humanReadableIdentifier: this.filters.humanReadableId.trim() || undefined,
            groupId: this.filters.groupId,
            dateOfBirth: undefined,
            enrollmentDate: undefined,
        };

        const filteredGroup = this.groups?.filter(g => g.id.toString() == this.filters.groupId)[0];
        this.appliedFilters.group = filteredGroup? filteredGroup.name : '';

        if(this.isRange(this.filters.enrollmentDateFrom, this.filters.enrollmentDateTo)){
            let enrollmentDateFrom = this.formatter.format(this.filters.enrollmentDateFrom); //this.formatDate(this.filters.enrollmentDateFrom);
            let enrollmentDateTo = this.formatter.format(this.filters.enrollmentDateTo);
            this.appliedFilters.enrollmentDateFrom = enrollmentDateFrom;
            this.appliedFilters.enrollmentDateTo = enrollmentDateTo;
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {
                from: enrollmentDateFrom ? enrollmentDateFrom + 'T00:00' + '[' + timeZone + ']' : undefined,
                to: enrollmentDateTo ? enrollmentDateTo + 'T23:59' + '[' + timeZone + ']' : undefined,
            };
        }
        if (this.filters.dateOfBirth && this.calendar.isValid(NgbDate.from(this.filters.dateOfBirth))){
            const dateOfBirth = this.formatter.format(this.filters.dateOfBirth);
            this.appliedFilters.dateOfBirth = dateOfBirth;
            params.dateOfBirth = {
                is: dateOfBirth,
            };
        }

        return params;
    }

    isRange(from: NgbDate, to: NgbDate): boolean {
        if(from && !this.calendar.isValid(NgbDate.from(from))){
            this.enrollmentDateFromError = true;
            return false;
        }
        if(to && !this.calendar.isValid(NgbDate.from(to))){
            this.enrollmentDateToError = true;
            return false;
        }
        if(from && to) {
            const dateFrom: NgbDate = new NgbDate(from.year, from.month, from.day);
            const dateTo: NgbDate = new NgbDate(to.year, to.month, to.day);
            if(dateTo.equals(dateFrom) || dateTo.after(dateFrom)){
                this.enrollmentDateFromError = false;
                this.enrollmentDateToError = false;
                return true;
            }
            this.enrollmentDateFromError = false;
            this.enrollmentDateToError = true;
            return false;
        } else {
            this.enrollmentDateFromError = false;
            this.enrollmentDateToError = false;
            return !!(from || to);
        }
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

    private onSuccess(data: Subject[], headers) {
        if(headers.get('link')){
            this.links = parseLinks(headers.get('link'));
        }
        this.totalItems = +headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        // remove redundant subjects from the list
        const fetchedSubjects = new Map<number, Subject>(data.map(a => [a.id, a]));
        this.updateSubjects([
            ...this.subjects.map(s => {
                const newSubject = fetchedSubjects.get(s.id);
                if (newSubject !== undefined) {
                    fetchedSubjects.delete(s.id);
                    return newSubject;
                } else {
                    return s;
                }
            }),
            ...data.filter(s => fetchedSubjects.has(s.id)),
        ]);
    }

    filterChanged(text) {
        this.filterTriggerUpdate$.next(text);
    }

    applyFilter() {
        const {subjectId, externalId, personName, humanReadableId, dateOfBirth, enrollmentDateFrom, enrollmentDateTo, groupId} = this.filters;
        this.isFilterApplied = !!(subjectId || externalId || personName || humanReadableId ||
                dateOfBirth || enrollmentDateFrom || enrollmentDateTo || groupId);
        this.clearSubjects();
        this.loadSubjects();
    }

    clearFilter(filterName: string){
        this.filters[filterName] = '';
        this.applyFilter();
    }

    clearDateFilter(filterName: string) {
        this.appliedFilters[filterName] = undefined;
        this.filters[filterName] = undefined;
        this.applyFilter();
    }

    clearSelectFilter(filterName: string){
        this.appliedFilters[filterName] = '';
        this.filters[filterName] = '';
        this.applyFilter();
    }

    clearFilters() {
        this.filters.externalId = '';
        this.filters.subjectId = '';
        this.filters.humanReadableId = '';
        this.filters.personName = '';
        this.filters.groupId = '';
        this.filters.dateOfBirth = undefined;
        this.filters.enrollmentDateFrom = undefined;
        this.filters.enrollmentDateTo = undefined;
        this.appliedFilters.group = '';
        this.appliedFilters.dateOfBirth = undefined;
        this.appliedFilters.enrollmentDateFrom = undefined;
        this.appliedFilters.enrollmentDateTo = undefined;

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

    private updateSubjects(newSubjects) {
        this.subjects = newSubjects;
        this.allChecked = this.subjects.length !== 0
          && this.subjects.every(({ id }) => this.setOfCheckedId.has(id));
    }

    private clearSubjects() {
        this.updateSubjects([]);
    }

    updateSortingSortBy(predicate) {
        this.clearSubjects();
        this.predicate = predicate;
        this.page = 1;
        this.transition();
    }

    updateSortingOrder(direction) {
        this.clearSubjects();
        this.ascending = direction === 'asc';
        this.page = 1;
        this.transition();
    }

    selectAll(checked: boolean = true): void {
        if (this.subjects.length === 0) {
            return;
        }
        this.subjects.forEach(({ id }) => this.updateCheckedSet(id, checked));
        this.allChecked = checked;
    }

    onItemChanged(id: number) {
        const nextIsChecked = !this.setOfCheckedId.has(id);
        this.updateCheckedSet(id, nextIsChecked);
        this.allChecked = nextIsChecked
          && this.subjects.every(({ id }) => this.setOfCheckedId.has(id));
    }

    updateCheckedSet(id: number, checked: boolean): void {
        if (checked) {
            this.setOfCheckedId.add(id);
        } else {
            this.setOfCheckedId.delete(id);
        }
    }

    addSelectedToGroup() {
        const selectedSubjects = this.subjects.filter(s => this.setOfCheckedId.has(s.id))
        const modalRef = this.modalService.open(AddSubjectsToGroupDialogComponent);
        modalRef.componentInstance.groups = this.groups;
        modalRef.componentInstance.projectName = this.project.projectName;
        modalRef.componentInstance.subjects = selectedSubjects;
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
