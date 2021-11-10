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
    switchMap, withLatestFrom
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
import { NgbDateReactiveFilter, ReactiveFilter } from "../util/reactive-filter";

interface FilterCriteria {
    externalId: string
    dateOfBirth?: NgbDate
    subjectId: string
    enrollmentDateFrom?: NgbDateStruct
    enrollmentDateTo?: NgbDateStruct
    groupId: string
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
    private filterResult$: Observable<FilterCriteria>;
    @Input()
    get project() { return this.project$.value; }
    set project(v: Project) { this.project$.next(v); }
    subjects$: BehaviorSubject<Subject[]> = new BehaviorSubject([]);
    groups$: BehaviorSubject<Group[]> = new BehaviorSubject([]);
    private subscriptions: Subscription = new Subscription();
    itemsPerPage = ITEMS_PER_PAGE;
    links: any;
    sortBy$: BehaviorSubject<string>;
    queryCount: any;
    ascending$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
    totalItems: number;
    page$: BehaviorSubject<number> = new BehaviorSubject<number>(1);
    previousPage: number = 1;

    filters: {[key: string]: ReactiveFilter<any>} = {
        subjectId: new ReactiveFilter<string>(),
        externalId: new ReactiveFilter<string>(),
        humanReadableId: new ReactiveFilter<string>(),
        dateOfBirth: new NgbDateReactiveFilter(this.calendar, this.formatter),
        personName: new ReactiveFilter<string>(),
        enrollmentDateFrom: new NgbDateReactiveFilter(this.calendar, this.formatter),
        enrollmentDateTo: new NgbDateReactiveFilter(this.calendar, this.formatter),
        groupId: new ReactiveFilter<string>({
            formatResult: v$ => v$.pipe(map(groupId => {
              const group = this.groups$.value.find(g => g.id.toString() == groupId);
              return group ? group.name : '';
            }))
        }),
    }

    isFilterApplied$: Observable<boolean>;

    enrollmentDateFromError = false;
    enrollmentDateToError = false;

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
        this.sortBy$ = new BehaviorSubject<string>('login')
        this.filterResult$ = combineLatest([
            this.filters.subjectId.value$,
            this.filters.externalId.value$,
            this.filters.humanReadableId.value$,
            this.filters.dateOfBirth.value$,
            this.filters.personName.value$,
            this.filters.enrollmentDateFrom.value$,
            this.filters.enrollmentDateTo.value$,
            this.filters.groupId.value$,
        ]).pipe(
          map(([subjectId, externalId, humanReadableId, dateOfBirth, personName, enrollmentDateFrom, enrollmentDateTo, groupId]) => ({
              subjectId,
              externalId,
              humanReadableId,
              dateOfBirth,
              personName,
              enrollmentDateFrom,
              enrollmentDateTo,
              groupId,
          })),
          shareReplay(1),
        )
        this.isFilterApplied$ = this.filterResult$
          .pipe(
              map(param => {
                  for (let key in param) {
                      if (param[key]) {
                          return true;
                      }
                  }
                  return false;
              })
          )

        this.subscriptions.add(this.activatedRoute.data.pipe(
          map(data => data['pagingParams']),
          filter(params => params),
        ).subscribe(params => {
            this.page$ = params.page;
            this.ascending$.next(params.ascending);
            this.sortBy$.next(params.predicate);
        }));

        this.checked$ = combineLatest([this.subjects$, this.setOfCheckedId$])
            .pipe(
              map(([subjects, checkedSet]) =>
                subjects.length !== 0 && subjects.every(v => checkedSet.has(v.id)))
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
            withLatestFrom(this.subjects$.pipe(map(s => s.length))),
            switchMap(([[projectName, filter, sortBy, ascending, page], numSubjects]) => {
                const mergeResults: boolean = page > this.previousPage;
                const numItems = Math.max(page * this.itemsPerPage - numSubjects, this.itemsPerPage);
                this.previousPage = page;
                let fetch$: Observable<HttpResponse<Subject[]>>;
                const filterParams = this.queryFilterParams(filter);
                const pagingParams = this.queryPaginationParams(sortBy, ascending, mergeResults, numItems)

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
            let enrollmentDateFrom = this.formatter.format(criteria.enrollmentDateFrom); //this.formatDate(this.filters.enrollmentDateFrom);
            let enrollmentDateTo = this.formatter.format(criteria.enrollmentDateTo);
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            params.enrollmentDate = {
                from: enrollmentDateFrom ? enrollmentDateFrom + 'T00:00' + '[' + timeZone + ']' : undefined,
                to: enrollmentDateTo ? enrollmentDateTo + 'T23:59' + '[' + timeZone + ']' : undefined,
            };
        }
        if (criteria.dateOfBirth){
            params.dateOfBirth = {
                is: this.formatter.format(criteria.dateOfBirth),
            };
        }

        return params;
    }

    isRange(from: NgbDateStruct, to: NgbDateStruct): boolean {
        if(from && to) {
            const dateFrom = NgbDate.from(from);
            const dateTo = NgbDate.from(to);
            return dateTo.equals(dateFrom) || dateTo.after(dateFrom);
        } else {
            return !!(from || to);
        }
    }

    queryPaginationParams(sortBy: string, ascending: boolean, loadMore: boolean, size: number): SubjectPaginationParams {
        const subjects = this.subjects$.value || [];

        let last: SubjectLastParams | null;
        if (loadMore) {
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
            size,
            sort: [sortBy + ',' + (ascending ? 'asc' : 'desc')],
        };
    }

    private onSuccess(data, headers, mergeResults: boolean) {
        if(headers.get('link')){
            this.links = parseLinks(headers.get('link'));
        }
        this.totalItems = +headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
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

    selectAll(): void {
        this.subjects$.pipe(
          withLatestFrom(this.checked$),
          first(),
        ).subscribe(([subjects, checked]) => {
            const nextValue = new Set(this.setOfCheckedId$.value);
            if (!checked) {
                subjects.forEach(({ id }) => nextValue.add(id));
            } else {
                subjects.forEach(({ id }) => nextValue.delete(id));
            }
            this.setOfCheckedId$.next(nextValue);
        });
    }

    onItemChecked(id: number, checked: boolean): void {
        const nextValue = new Set(this.setOfCheckedId$.value);
        if (checked) {
            nextValue.add(id);
        } else {
            nextValue.delete(id);
        }
        this.setOfCheckedId$.next(nextValue);
    }

    addSelectedToGroup() {
        // TODO implement function
    }
}
