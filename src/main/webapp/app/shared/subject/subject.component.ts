import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import {
    BehaviorSubject,
    Observable,
    Subscription,
    combineLatest
} from 'rxjs';
import {
    debounceTime,
    distinctUntilChanged,
    filter,
    first,
    map,
    pluck,
    shareReplay,
    switchMap, tap,
    withLatestFrom,
} from 'rxjs/operators';
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

import {Group, GroupService, ITEMS_PER_PAGE, Project} from '..';
import {
    AddSubjectsToGroupDialogComponent
} from "./add-subjects-to-group-dialog.component";
import { Subject, SubjectFilterCriteria } from './subject.model';
import {
    SubjectService,
    SubjectFilterParams,
    SubjectPaginationParams,
} from './subject.service';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import {
    NgbCalendar,
    NgbDateParserFormatter,
} from "@ng-bootstrap/ng-bootstrap";
import {
    NgbDateRange,
    NgbDateReactiveFilter,
    ReactiveFilter,
    ReactiveFilterOptions
} from "../util/reactive-filter";

@Component({
    selector: 'jhi-subjects',
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
    subjects$: BehaviorSubject<Subject[]> = new BehaviorSubject([]);
    groups$: BehaviorSubject<Group[]> = new BehaviorSubject([]);

    page$ = new BehaviorSubject<number>(1);
    totalItems: number;
    previousPage: number = 1;
    itemsPerPage = ITEMS_PER_PAGE;

    sortBy$ = new BehaviorSubject<string>('login');
    ascending$ = new BehaviorSubject<boolean>(true);
    sortingOptions = [
        'login',
        'externalId',
    ];

    filters: Record<string, ReactiveFilter<any>>
    enrollmentDate$: Observable<NgbDateRange>
    enrollmentDateRangeError = false;
    filterResult$: Observable<SubjectFilterCriteria>;

    isAdvancedFilterCollapsed = true;

    setOfCheckedId$ = new BehaviorSubject<Set<number>>(new Set());
    allChecked$: Observable<boolean>;
    anyChecked$: Observable<boolean>;

    constructor(
            private subjectService: SubjectService,
            private groupService: GroupService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private modalService: NgbModal,
            private activatedRoute: ActivatedRoute,
            private router: Router,
            calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter
    ) {
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
            dateOfBirth: new NgbDateReactiveFilter(calendar, this.formatter),
            personName: new ReactiveFilter<string>(stringFilterOptions),
            enrollmentDateFrom: new NgbDateReactiveFilter(calendar, this.formatter),
            enrollmentDateTo: new NgbDateReactiveFilter(calendar, this.formatter),
            groupId: new ReactiveFilter<number>({ debounceTime: 1 }),
        }
        this.enrollmentDate$ = this.observeEnrollmentDate();
        this.filterResult$ = this.observeCombinedFilters();

        this.subscriptions.add(this.registerChangeInQueryParams());

        this.allChecked$ = this.observeChecked(([subjects, checkedSet]) =>
          subjects.length !== 0 && subjects.every(v => checkedSet.has(v.id))
        );
        this.anyChecked$ = this.observeChecked(([subjects, checkedSet]) =>
          subjects.some(v => checkedSet.has(v.id))
        );
    }

    ngOnInit() {
        this.subscriptions.add(this.registerChangeInFilters());
        this.subscriptions.add(this.registerChangeInSubjects());
        if (this.isProjectSpecific) {
            this.subscriptions.add(this.loadAllGroups());
        }
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
        this.project$.complete();
        this.subjects$.complete();
        this.groups$.complete();
        this.page$.complete();
        this.sortBy$.complete();
        this.ascending$.complete();
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

    private registerChangeInQueryParams(): Subscription {
        return this.activatedRoute.data.pipe(
          pluck('pagingParams'),
        ).subscribe(params => {
            this.page$.next(params.page);
            this.ascending$.next(params.ascending);
            if (params.predicate in this.sortingOptions) {
                this.sortBy$.next(params.predicate);
            }
            for (let k in params) {
                if (params.hasOwnProperty(k) && this.filters[k]) {
                    this.filters[k].next(params[k]);
                }
            }
        })
    }

    private registerChangeInFilters(): Subscription {
        return combineLatest([
            this.project$.pipe(map(p => p ? p.projectName : ''), distinctUntilChanged()),
            this.filterResult$,
            this.sortBy$.pipe(distinctUntilChanged()),
            this.ascending$.pipe(distinctUntilChanged()),
            this.page$.pipe(distinctUntilChanged()),
        ]).pipe(
          debounceTime(10),
          tap(([projectName, criteria, sortBy, ascending, page]) =>
            this.router.navigate([], {
                relativeTo: this.activatedRoute,
                queryParams: this.toQueryParams(criteria, page, sortBy, ascending),
            })),
          withLatestFrom(this.subjects$),
          switchMap(([[projectName, filter, sortBy, ascending, page], subjects]) => {
              const mergeResults: boolean = page > this.previousPage;
              this.previousPage = page;
              const filterParams = this.queryFilterParams(filter);
              const pagingParams = this.queryPaginationParams(page, sortBy, ascending, mergeResults, subjects)

              let fetch$: Observable<HttpResponse<Subject[]>>;
              if (this.isProjectSpecific) {
                  fetch$ = this.subjectService.findAllByProject(
                    projectName,
                    filterParams,
                    pagingParams,
                  );
              } else {
                  fetch$ = this.subjectService.query(filterParams, pagingParams);
              }
              return fetch$.pipe(
                map(res => ({
                    body: res.body,
                    headers: res.headers,
                    mergeResults: mergeResults,
                }))
              );
          })
        )
        .subscribe(res => this.onSuccess(res.body, res.headers, res.mergeResults));
    }

    private registerChangeInSubjects(): Subscription {
        return this.eventManager.subscribe('subjectListModification', (result) => {
          const modifiedSubject = result.content;
          let currentSubjects = this.subjects$.value;
          const subjectIndex = currentSubjects.findIndex((s => s.id == modifiedSubject.id));
          if (subjectIndex < 0) {
              this.totalItems++;
              currentSubjects = [modifiedSubject, ...currentSubjects];
          } else {
              currentSubjects = currentSubjects.slice();
              currentSubjects[subjectIndex] = modifiedSubject;
          }
          this.subjects$.next(currentSubjects);
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
                params.enrollmentDate.from = this.formatter.format(criteria.enrollmentDateFrom)
                  + 'T00:00' + '[' + timeZone + ']';
            }
            if (criteria.enrollmentDateTo) {
                params.enrollmentDate.to = this.formatter.format(criteria.enrollmentDateTo)
                  + 'T23:59' + '[' + timeZone + ']';
            }
        }
        if (criteria.dateOfBirth) {
            params.dateOfBirth = {
                is: this.formatter.format(criteria.dateOfBirth),
            };
        }

        return params;
    }

    private queryPaginationParams(page: number, sortBy: string, ascending: boolean, loadMore: boolean, subjects: Subject[]): SubjectPaginationParams {
        const params: SubjectPaginationParams = {
            size: Math.max(page * this.itemsPerPage - subjects.length, this.itemsPerPage),
            sort: [sortBy + ',' + (ascending ? 'asc' : 'desc')],
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
              ...this.subjects$.value.map(s => {
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
        this.subjects$.next(nextValue);
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
        this.sortBy$.next(predicate);
        this.page$.next(1);
    }

    updateSortAscending(ascending: boolean) {
        this.ascending$.next(ascending);
        this.page$.next(1);
    }

    toggleSelectAll(): void {
        combineLatest([
            this.subjects$,
            this.setOfCheckedId$,
        ]).pipe(
          first(),
          filter(([subjects]) => subjects.length > 0),
          map(([subjects, checkedIds]) => {
              const nextValue = new Set(checkedIds);
              if (subjects.every(s => checkedIds.has(s.id))) {
                  subjects.forEach(({id}) => nextValue.delete(id));
              } else {
                  subjects.forEach(({id}) => nextValue.add(id));
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
              groupName: this.groups$.value.find(g => g.id.toString() == groupId)?.name,
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

    private observeChecked<T>(modify: (state: [Subject[], Set<number>]) => T): Observable<T> {
        return combineLatest([this.subjects$, this.setOfCheckedId$]).pipe(
          map(v => modify(v)),
          distinctUntilChanged(),
        );
    }

    private toQueryParams(
      filters: SubjectFilterCriteria | null,
      page: number,
      sortBy: string,
      ascending: boolean,
    ): Record<string, string> {
        let params = {
            page: page.toString(),
            sort: sortBy + ',' + (ascending ? 'asc' : 'desc'),
        }
        if (filters) {
            const stringFilters: Record<string, any> = Object.assign<Record<string, any>, SubjectFilterCriteria>({}, filters);
            delete stringFilters['groupName'];
            if (filters.enrollmentDateFrom) {
                stringFilters.enrollmentDateFrom = this.formatter.format(filters.enrollmentDateFrom);
            }
            if (filters.enrollmentDateTo) {
                stringFilters.enrollmentDateTo = this.formatter.format(filters.enrollmentDateTo);
            }
            if (filters.dateOfBirth) {
                stringFilters.dateOfBirth = this.formatter.format(filters.dateOfBirth);
            }
            for (let k in stringFilters) {
                if (stringFilters[k]) {
                    params[k] = encodeURIComponent(stringFilters[k]);
                }
            }
        }
        return params;
    }

    addSelectedToGroup() {
        const modalRef = this.modalService.open(AddSubjectsToGroupDialogComponent);
        this.subscriptions.add(combineLatest([
          this.subjects$,
          this.setOfCheckedId$,
          this.groups$,
          this.project$,
        ]).subscribe(([subjects, checkedIds, groups, project]) => {
          modalRef.componentInstance.groups = groups;
          modalRef.componentInstance.projectName = project.projectName;
          modalRef.componentInstance.subjects = subjects.filter(s => checkedIds.has(s.id));
        }));
    }
}
