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
import {
    BehaviorSubject,
    Observable,
    Subscription,
    combineLatest
} from 'rxjs';
import {
    distinctUntilChanged, filter, first,
    map, pluck,
    shareReplay,
    switchMap, tap, withLatestFrom
} from 'rxjs/operators';

import {Group, GroupService, ITEMS_PER_PAGE, Project} from '..';
import { Subject } from './subject.model';
import {
    SubjectService,
    SubjectFilterParams,
    SubjectPaginationParams, SubjectLastParams,
} from './subject.service';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { parseLinks } from '../util/parse-links-util';
import {
    NgbCalendar,
    NgbDate,
    NgbDateParserFormatter,
    NgbDateStruct
} from "@ng-bootstrap/ng-bootstrap";
import { NgbDateRange, NgbDateReactiveFilter, ReactiveFilter } from "../util/reactive-filter";

interface FilterCriteria {
    externalId: string
    dateOfBirth?: NgbDate
    subjectId: string
    enrollmentDateFrom?: NgbDateStruct
    enrollmentDateTo?: NgbDateStruct
    groupId: string
    groupName: string
    personName: string
    humanReadableId: string
}

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
    project$ = new BehaviorSubject<Project>(null);
    filterResult$: Observable<FilterCriteria>;
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    subjects$: BehaviorSubject<Subject[]> = new BehaviorSubject([]);
    groups$: BehaviorSubject<Group[]> = new BehaviorSubject([]);
    private subscriptions: Subscription = new Subscription();
    itemsPerPage = ITEMS_PER_PAGE;
    links: any;
    sortBy$: BehaviorSubject<string>;
    ascending$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
    totalItems: number;
    page$: BehaviorSubject<number> = new BehaviorSubject<number>(1);
    previousPage: number = 1;
    enrollmentDate$: Observable<NgbDateRange>
    enrollmentDateRangeError = false;

    filters: Record<string, ReactiveFilter<any>>

    isAdvancedFilterCollapsed = true;

    checked$: Observable<boolean>;
    setOfCheckedId$: BehaviorSubject<Set<number>> = new BehaviorSubject<Set<number>>(new Set());

    @Input() readonly isProjectSpecific: boolean;

    constructor(
            private subjectService: SubjectService,
            private groupService: GroupService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private activatedRoute: ActivatedRoute,
            private router: Router,
            private calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter
    ) {
        this.filters = {
            subjectId: new ReactiveFilter<string>(),
            externalId: new ReactiveFilter<string>(),
            humanReadableId: new ReactiveFilter<string>(),
            dateOfBirth: new NgbDateReactiveFilter(this.calendar, this.formatter),
            personName: new ReactiveFilter<string>(),
            enrollmentDateFrom: new NgbDateReactiveFilter(this.calendar, this.formatter),
            enrollmentDateTo: new NgbDateReactiveFilter(this.calendar, this.formatter),
            groupId: new ReactiveFilter<string>(),
        }
        this.enrollmentDate$ = combineLatest([
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
        );
        this.sortBy$ = new BehaviorSubject<string>('login')
        this.filterResult$ = combineLatest([
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
          map(criteria => {
              for (let key in criteria) {
                  if (criteria[key]) {
                      return criteria;
                  }
              }
              return null;
          }),
          shareReplay(1),
        )

        this.subscriptions.add(this.activatedRoute.data.pipe(
          pluck('pagingParams'),
        ).subscribe(params => {
            this.page$.next(params.page);
            this.ascending$.next(params.ascending);
            if (params.predicate in this.sortingOptions) {
                this.sortBy$.next(params.predicate);
            } else {
                this.sortBy$.next(this.sortingOptions[0]);
            }
        }));

        this.checked$ = combineLatest([this.subjects$, this.setOfCheckedId$])
            .pipe(
              map(([subjects, checkedSet]) =>
                subjects.length !== 0 && subjects.every(v => checkedSet.has(v.id))),
              distinctUntilChanged(),
            );
    }

    ngOnInit() {
        this.subscriptions.add(
          combineLatest([
              this.project$.pipe(map(p => p ? p.projectName : ''), distinctUntilChanged()),
              this.filterResult$,
              this.sortBy$.pipe(distinctUntilChanged()),
              this.ascending$.pipe(distinctUntilChanged()),
              this.page$.pipe(distinctUntilChanged()),
          ]).pipe(
            withLatestFrom(this.subjects$),
            switchMap(([[projectName, filter, sortBy, ascending, page], subjects]) => {
                const mergeResults: boolean = page > this.previousPage;
                this.previousPage = page;
                let fetch$: Observable<HttpResponse<Subject[]>>;
                const filterParams = this.queryFilterParams(filter);
                const pagingParams = this.queryPaginationParams(page, sortBy, ascending, mergeResults, subjects)

                if (this.isProjectSpecific) {
                    fetch$ = this.subjectService.findAllByProject(
                      projectName,
                      filterParams,
                      pagingParams,
                    );
                } else {
                    fetch$ = this.subjectService.query(filterParams, pagingParams,);
                }
                this.router.navigate([], {
                    replaceUrl: true,
                    relativeTo: this.activatedRoute,
                    queryParams: {
                        page: page,
                        sort: sortBy + ',' + (ascending ? 'asc' : 'desc'),
                    },
                });
               return fetch$.pipe(
                  map(res => ({
                    body: res.body,
                    headers: res.headers,
                    mergeResults: mergeResults,
                  }))
                );
            })
          )
          .subscribe(
            res => {
                this.onSuccess(res.body, res.headers, res.mergeResults);
            },
            (res: HttpErrorResponse) => this.onError(res),
          )
        );
        this.registerChangeInSubjects();
        if (this.isProjectSpecific) {
            this.loadAllGroups();
        }
    }

    ngOnDestroy() {
        for (let filtersKey in this.filters) {
            this.filters[filtersKey].complete();
        }
        this.ascending$.complete();
        this.sortBy$.complete();
        this.subjects$.complete();
        this.project$.complete();
        this.page$.complete();
        this.setOfCheckedId$.complete();
        this.subscriptions.unsubscribe();
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

    registerChangeInSubjects() {
        this.subscriptions.add(
          this.eventManager.subscribe('subjectListModification', (result) => {
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
          })
        );
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    private loadAllGroups() {
        this.subscriptions.add(
          this.project$.pipe(
            filter(p => !!p),
            pluck('projectName'),
            switchMap(projectName => this.groupService.list(projectName))
          ).subscribe(
            (res: Group[]) => this.groups$.next(res),
            (res: HttpErrorResponse) => this.onError(res),
          )
        );
    }

    queryFilterParams(criteria: FilterCriteria): SubjectFilterParams {
        if (!criteria) {
            return {};
        }
        const params = {
            login: criteria.subjectId && criteria.subjectId.trim() || undefined,
            externalId: criteria.externalId && criteria.externalId.trim() || undefined,
            personName: criteria.personName && criteria.personName.trim() || undefined,
            humanReadableIdentifier: criteria.humanReadableId && criteria.humanReadableId.trim() || undefined,
            groupId: criteria.groupId,
            dateOfBirth: undefined,
            enrollmentDate: undefined,
        };

        if (this.isRange(criteria.enrollmentDateFrom, criteria.enrollmentDateTo)){
            let enrollmentDateFrom = this.formatter.format(criteria.enrollmentDateFrom);
            let enrollmentDateTo = this.formatter.format(criteria.enrollmentDateTo);
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {
                from: enrollmentDateFrom ? enrollmentDateFrom + 'T00:00' + '[' + timeZone + ']' : undefined,
                to: enrollmentDateTo ? enrollmentDateTo + 'T23:59' + '[' + timeZone + ']' : undefined,
            };
        }
        if (criteria.dateOfBirth) {
            params.dateOfBirth = {
                is: this.formatter.format(criteria.dateOfBirth),
            };
        }

        return params;
    }

    isRange(from: NgbDateStruct, to: NgbDateStruct): boolean {
        if (from && to) {
            const dateFrom = NgbDate.from(from);
            const dateTo = NgbDate.from(to);
            return dateTo.equals(dateFrom) || dateTo.after(dateFrom);
        } else {
            return !!(from || to);
        }
    }

    queryPaginationParams(page: number, sortBy: string, ascending: boolean, loadMore: boolean, subjects: Subject[]): SubjectPaginationParams {
        let last: SubjectLastParams | null;
        if (loadMore && subjects.length > 0) {
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
            size: Math.max(page * this.itemsPerPage - subjects.length, this.itemsPerPage),
            sort: [sortBy + ',' + (ascending ? 'asc' : 'desc')],
        };
    }

    private onSuccess(data, headers, mergeResults: boolean) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = +headers.get('X-Total-Count');
        // remove redundant subjects from the list
        if (mergeResults) {
            let tempSubjects = [...this.subjects$.value, ...data];
            this.subjects$.next(Array.from(new Set(tempSubjects.map(a => a.id)))
            .map(id => {
                return tempSubjects.find(a => a.id === id)
            }))
        } else {
            this.subjects$.next(data);
        }
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

    addSelectedToGroup() {
        // TODO implement function
    }
}
