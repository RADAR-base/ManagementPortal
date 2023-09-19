import { ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges, } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, first, map, pluck, shareReplay, switchMap, tap, withLatestFrom, } from 'rxjs/operators';
import { NgbCalendar, NgbDateParserFormatter, NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { Group, GroupService, ITEMS_PER_PAGE, Project } from '..';
import { AddSubjectsToGroupDialogComponent } from './add-subjects-to-group-dialog.component';
import {CheckedSubject, SiteSettings, Subject, SubjectFilterCriteria} from './subject.model';
import { SubjectFilterParams, SubjectPaginationParams, SubjectService, } from './subject.service';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { NgbDateRange, NgbDateReactiveFilter, ReactiveFilter, ReactiveFilterOptions } from '../util/reactive-filter';
import { regularSortOrder, SortOrder, SortOrderImpl } from '../util/sort-util';
import {SiteSettingsService} from "./sitesettings.service";

@Component({
    selector: 'jhi-subjects',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './subject.component.html',
    styleUrls: ['./subject.component.scss'],
})
export class SubjectComponent implements OnInit, OnDestroy, OnChanges {
    private subscriptions: Subscription = new Subscription();

    @Input() readonly isProjectSpecific: boolean;

    project$ = new BehaviorSubject<Project>(null);
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    _subjects$: BehaviorSubject<Subject[]> = new BehaviorSubject([]);
    subjects$: Observable<CheckedSubject[]>
    groups$: BehaviorSubject<Group[]> = new BehaviorSubject([]);

    page$ = new BehaviorSubject<number>(1);
    totalItems: number;
    previousPage: number = 1;
    itemsPerPage = ITEMS_PER_PAGE;

    private _sortOrder$ = new BehaviorSubject<SortOrder>({
        predicate: 'login',
        ascending: true,
    });
    sortOrder$: Observable<SortOrderImpl>;

    sortingOptions = [
        'login',
        'externalId',
    ];

    filters: Record<string, ReactiveFilter<any>>
    enrollmentDate$: Observable<NgbDateRange>
    enrollmentDateRangeError = false;
    filterResult$: Observable<SubjectFilterCriteria>;
    formattedFilterResult$: Observable<Record<string, string>>;
    isAdvancedFilterCollapsed = true;
    public siteSettings$: Observable<SiteSettings>;

    setOfCheckedId$ = new BehaviorSubject<Set<number>>(new Set());
    allChecked$: Observable<boolean>;
    anyChecked$: Observable<boolean>;

    constructor(
        private subjectService: SubjectService,
        private groupService: GroupService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private modalService: NgbModal,
        private siteSettingsService: SiteSettingsService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        calendar: NgbCalendar,
        public dateFormatter: NgbDateParserFormatter
    ) {
        this.sortOrder$ = this._sortOrder$.pipe(regularSortOrder());

        const stringFilterOptions: ReactiveFilterOptions<string> = {
            mapResult: filter$ => filter$.pipe(
                map(v => v ? v.trim() : ''),
                distinctUntilChanged(),
            ),
        }
        this.filters = {
            subjectId: new ReactiveFilter<string>(stringFilterOptions),
            externalId: new ReactiveFilter<string>(stringFilterOptions),
            humanReadableId: new ReactiveFilter<string>(stringFilterOptions),
            dateOfBirth: new NgbDateReactiveFilter(calendar, this.dateFormatter),
            personName: new ReactiveFilter<string>(stringFilterOptions),
            enrollmentDateFrom: new NgbDateReactiveFilter(calendar, this.dateFormatter),
            enrollmentDateTo: new NgbDateReactiveFilter(calendar, this.dateFormatter),
            groupId: new ReactiveFilter<number>({ debounceTime: 1 }),
        }
        this.enrollmentDate$ = this.observeEnrollmentDate();
        this.filterResult$ = this.observeCombinedFilters();
        this.formattedFilterResult$ = this.filterResult$.pipe(
            map((f) => {
                if (!f) return null;
                let {dateOfBirth, enrollmentDateFrom, enrollmentDateTo, groupId, ...filters} = f;
                return {
                    dateOfBirth: this.dateFormatter.format(dateOfBirth),
                    enrollmentDateFrom: this.dateFormatter.format(enrollmentDateFrom),
                    enrollmentDateTo: this.dateFormatter.format(enrollmentDateTo),
                    groupId: this.groups$.value.find(g => g.id.toString() == groupId)?.name,
                    ...filters,
                }
            }),
        );
        this.filterResult$ = this.observeCombinedFilters();

        this.subjects$ = combineLatest([
            this._subjects$,
            this.setOfCheckedId$,
        ]).pipe(
            map(([subjects, checkedSet]) =>
                subjects.map(s => ({...s, checked: checkedSet.has(s.id) }))),
            shareReplay(1),
        )
        this.allChecked$ = this.subjects$.pipe(
            map(subjects => subjects.length !== 0 && subjects.every(s => s.checked)),
            distinctUntilChanged(),
        );
        this.anyChecked$ = this.subjects$.pipe(
            map(subjects => subjects.some(s => s.checked)),
            distinctUntilChanged(),
        );
        this.siteSettings$ = this.siteSettingsService.getSiteSettings();


        this.subscriptions.add(this.registerChangeInPagingParams());
        this.subscriptions.add(this.registerChangeInParams());
    }

    ngOnInit() {
        this.subscriptions.add(this.registerChangeInFilters());
        this.subscriptions.add(this.registerChangeInSubjects());
        this.subscriptions.add(this.loadAllGroups());
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
        this.project$.complete();
        this._subjects$.complete();
        this.groups$.complete();
        this.page$.complete();
        this._sortOrder$.complete();
        for (let filtersKey in this.filters) {
            this.filters[filtersKey].complete();
        }
        this.setOfCheckedId$.complete();
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
        }
    }

    trackLogin(index: number, item: Subject) {
        return item.login;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }

    private registerChangeInParams(): Subscription {
        return this.activatedRoute.params.pipe(
            first(),
        ).subscribe(params => Object.entries(params).forEach(([k, v]) => {
            if (this.filters.hasOwnProperty(k)) {
                this.filters[k].next(v, true);
            }
        }));
    }

    private registerChangeInPagingParams(): Subscription {
        return this.activatedRoute.data.pipe(
          pluck('pagingParams'),
          first(),
        ).subscribe(params => {
            this.page$.next(params.page);
            this._sortOrder$.next({
                predicate: params.predicate,
                ascending: params.ascending,
            });
        })
    }

    private registerChangeInFilters(): Subscription {
        return combineLatest([
            this.project$.pipe(map(p => p ? p.projectName : ''), distinctUntilChanged()),
            this.filterResult$,
            this.sortOrder$,
            this.page$.pipe(distinctUntilChanged()),
        ]).pipe(
          debounceTime(10),
          tap(([projectName, criteria, sortOrder, page]) =>
            this.router.navigate(this.toPathParams(projectName, criteria), {
                queryParams: {
                    page: page.toString(),
                    sort: sortOrder.toQueryParam(),
                },
                queryParamsHandling: "merge",
                replaceUrl: true,
            })),
          withLatestFrom(this._subjects$),
          switchMap(([[projectName, filter, sortOrder, page], subjects]) => {
              const mergeResults: boolean = page > this.previousPage;
              this.previousPage = page;
              const filterParams = this.queryFilterParams(filter);
              const pagingParams = this.queryPaginationParams(page, sortOrder, mergeResults, subjects)

              let fetch$: Observable<HttpResponse<Subject[]>>;
              if (projectName) {
                  fetch$ = this.subjectService.findAllByProject(projectName, filterParams, pagingParams);
              } else {
                  fetch$ = this.subjectService.query(filterParams, pagingParams);
              }
              return fetch$.pipe(
                map(res => ({
                    body: res.body,
                    headers: res.headers,
                    mergeResults: mergeResults,
                })),
              );
          })
        )
        .subscribe(res => this.onSuccess(res.body, res.headers, res.mergeResults));
    }

    private registerChangeInSubjects(): Subscription {
        return this.eventManager.subscribe('subjectListModification', ({content}) => {
            const modifiedSubject = content.subject;
            let currentSubjects = this._subjects$.value.slice();
            const subjectIndex = currentSubjects.findIndex((s => s.login === modifiedSubject.login));
            if (content.op === 'DELETE') {
                if (subjectIndex >= 0) {
                    currentSubjects = currentSubjects.splice(subjectIndex, 1);
                }
                this.totalItems--;
            } else if (subjectIndex >= 0) {
                currentSubjects[subjectIndex] = modifiedSubject;
            } else {
                this.totalItems++;
                currentSubjects = [modifiedSubject, ...currentSubjects];
            }
            this._subjects$.next(currentSubjects);
        });
    }

    private loadAllGroups() {
        return this.project$.pipe(
          filter(p => !!p),
          pluck('projectName'),
          distinctUntilChanged(),
          switchMap(projectName => this.groupService.list(projectName))
        ).subscribe((res: Group[]) => this.groups$.next(res));
    }

    queryFilterParams(criteria: SubjectFilterCriteria): SubjectFilterParams {
        if (!criteria) {
            return {};
        }
        const params: SubjectFilterParams = {
            login: criteria.subjectId || undefined,
            externalId: criteria.externalId || undefined,
            personName: criteria.personName || undefined,
            humanReadableIdentifier: criteria.humanReadableId || undefined,
            groupId: criteria.groupId || undefined,
        };

        if (criteria.enrollmentDateFrom || criteria.enrollmentDateTo) {
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {};
            if (criteria.enrollmentDateFrom) {
                params.enrollmentDate.from = this.dateFormatter.format(criteria.enrollmentDateFrom)
                  + 'T00:00' + '[' + timeZone + ']';
            }
            if (criteria.enrollmentDateTo) {
                params.enrollmentDate.to = this.dateFormatter.format(criteria.enrollmentDateTo)
                  + 'T23:59' + '[' + timeZone + ']';
            }
        }
        if (criteria.dateOfBirth) {
            params.dateOfBirth = {
                is: this.dateFormatter.format(criteria.dateOfBirth),
            };
        }

        return params;
    }

    private queryPaginationParams(page: number, sortOrder: SortOrderImpl, loadMore: boolean, subjects: Subject[]): SubjectPaginationParams {
        const params: SubjectPaginationParams = {
            size: Math.max(page * this.itemsPerPage - subjects.length, this.itemsPerPage),
            sort: [sortOrder.toQueryParam()],
        };
        if (loadMore && subjects.length > 0) {
            const lastSubject = subjects[subjects.length - 1]
            params.last = {
                id: lastSubject.id,
                login: lastSubject.login,
                externalId: lastSubject.externalId || '',
            }
        }
        return params;
    }

    private onSuccess(data: Subject[], headers: HttpHeaders, mergeResults: boolean) {
        this.totalItems = +headers.get('X-Total-Count');
        // remove redundant subjects from the list
        let nextValue: Subject[]
        if (mergeResults) {
            const fetchedSubjects = new Map<number, Subject>(data.map(a => [a.id, a]));
            nextValue = [
              ...this._subjects$.value.map(s => {
                  const newSubject = fetchedSubjects.get(s.id);
                  if (newSubject) {
                      fetchedSubjects.delete(s.id);
                      return newSubject;
                  } else {
                      return s;
                  }
              }),
              ...data.filter(s => fetchedSubjects.has(s.id)),
            ];
        } else {
            nextValue = data;
        }
        this._subjects$.next(nextValue);
    }

    clearFilters() {
        for (let filtersKey in this.filters) {
            this.filters[filtersKey].clear();
        }
    }

    loadMore() {
        this.page$.next(this.page$.value + 1);
    }

    updateSortingSortBy(predicate?: string) {
        this._sortOrder$.next({
            predicate: predicate || this._sortOrder$.value.predicate,
            ascending: this._sortOrder$.value.ascending,
        });
        this.page$.next(1);
    }

    updateSortAscending(ascending: boolean) {
        this._sortOrder$.next({
            predicate: this._sortOrder$.value.predicate,
            ascending: ascending,
        });
        this.page$.next(1);
    }

    toggleSelectAll(): void {
        combineLatest([
            this._subjects$,
            this.setOfCheckedId$,
        ]).pipe(
          first(),
          filter(([subjects]) => subjects.length > 0),
          map(([subjects, checkedIds]) => {
              const nextValue = new Set(checkedIds);
              if (subjects.every(s => checkedIds.has(s.id))) {
                  subjects.forEach(({id}) => nextValue.delete(id));
              } else {
                  subjects.forEach(({id, status}) => {
                      if(status.toString() === "ACTIVATED"){
                        nextValue.add(id)
                      }
                  });
              }
              return nextValue;
          })
        ).subscribe(nextValue => this.setOfCheckedId$.next(nextValue));
    }

    onItemChecked(id: number, checked: boolean): void {
        this.setOfCheckedId$.pipe(
          first(),
          map(v => {
              const nextValue = new Set(v);
              if (checked) {
                  nextValue.add(id);
              } else {
                  nextValue.delete(id);
              }
              return nextValue;
          }),
        ).subscribe(nextValue => this.setOfCheckedId$.next(nextValue));
    }

    private observeCombinedFilters(): Observable<SubjectFilterCriteria> {
        return combineLatest([
            this.filters.subjectId.value$,
            this.filters.externalId.value$,
            this.filters.humanReadableId.value$,
            this.filters.dateOfBirth.value$,
            this.filters.personName.value$,
            this.enrollmentDate$,
            this.filters.groupId.value$,
        ]).pipe(
          map(([subjectId, externalId, humanReadableId, dateOfBirth, personName, enrollmentDate, groupId]) => ({
              subjectId,
              externalId,
              humanReadableId,
              dateOfBirth,
              personName,
              enrollmentDateFrom: enrollmentDate.from,
              enrollmentDateTo: enrollmentDate.to,
              groupId,
          })),
          map(criteria =>
            Object.keys(criteria).some(k => !!criteria[k]) ? criteria : null),
          shareReplay(1),
        )
    }

    private observeEnrollmentDate(): Observable<NgbDateRange> {
        return combineLatest([
            this.filters.enrollmentDateFrom.value$,
            this.filters.enrollmentDateTo.value$,
        ]).pipe(
          map(([from, to]) => ({from, to})),
          filter(({from, to}) => {
              if (NgbDateReactiveFilter.isValidRange(from, to)) {
                  this.enrollmentDateRangeError = false;
                  return true;
              } else {
                  this.enrollmentDateRangeError = true;
                  return false;
              }
          }),
        )
    }

    private toPathParams(projectName: string, criteria: SubjectFilterCriteria): Record<string, string>[] {
        let route = [];
        if (projectName) {
            route.push('/project');
            route.push(projectName);
        } else {
            route.push('subject')
        }
        if (criteria) {
            const stringFilters: Record<string, any> = Object.assign<Record<string, any>, SubjectFilterCriteria>({}, criteria);
            delete stringFilters['groupName'];
            if (criteria.enrollmentDateFrom) {
                stringFilters.enrollmentDateFrom = this.dateFormatter.format(criteria.enrollmentDateFrom);
            }
            if (criteria.enrollmentDateTo) {
                stringFilters.enrollmentDateTo = this.dateFormatter.format(criteria.enrollmentDateTo);
            }
            if (criteria.dateOfBirth) {
                stringFilters.dateOfBirth = this.dateFormatter.format(criteria.dateOfBirth);
            }
            let params = {};
            for (let k in stringFilters) {
                if (stringFilters[k]) {
                    params[k] = encodeURIComponent(stringFilters[k]);
                }
            }
            route.push(params);
        }
        return route;
    }

    addSelectedToGroup() {
        const modalRef = this.modalService.open(AddSubjectsToGroupDialogComponent);
        this.subscriptions.add(combineLatest([
          this.subjects$,
          this.groups$,
          this.project$,
        ]).subscribe(([subjects, groups, project]) => {
            if(modalRef.componentInstance){
                modalRef.componentInstance.groups = groups;
                modalRef.componentInstance.projectName = project.projectName;
                modalRef.componentInstance.subjects = subjects.filter(s => s.checked);
            }
        }));
    }
}
