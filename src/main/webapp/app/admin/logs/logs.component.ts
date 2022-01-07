import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';

import { Log } from './log.model';
import { LogsService } from './logs.service';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, map, share, shareReplay, switchMap, tap } from 'rxjs/operators';
import { regularSortOrder, SortOrder, SortOrderImpl } from '../../shared/util/sort-util';
import { ITEMS_PER_PAGE } from '../../shared';

@Component({
    selector: 'jhi-logs',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './logs.component.html',
})
export class LogsComponent implements OnInit, OnDestroy {
    private _loggers$: BehaviorSubject<Log[]> = new BehaviorSubject([]);
    loggerView$: Observable<Log[]>
    loggersFiltered$: Observable<Log[]>
    filter$ = new BehaviorSubject<string>('');
    sortOrder$: Observable<SortOrderImpl>;
    page$: BehaviorSubject<number> = new BehaviorSubject(0);
    itemsPerPage: number;

    private _sortOrder$ = new BehaviorSubject<SortOrder>({predicate: '', ascending: true});
    private subscriptions = new Subscription();

    constructor(
        private logsService: LogsService,
    ) {
        this.sortOrder$ = this._sortOrder$.pipe(regularSortOrder('name'));
        this.itemsPerPage = ITEMS_PER_PAGE;

        this.loggersFiltered$ = this.filterLoggers(this._loggers$).pipe(
            shareReplay({
                bufferSize: 1,
                refCount: true,
            }),
        );

        const loggerSorted$ = this.sortLoggers(this.loggersFiltered$);
        this.loggerView$ = this.pageLoggers(loggerSorted$);
    }

    private filterLoggers(loggers$: Observable<Log[]>): Observable<Log[]> {
        return combineLatest([
            loggers$,
            this.cleanedFilter$,
        ]).pipe(
            map(([loggers, filters]) => {
                if (filters.length === 0) {
                    return loggers;
                } else {
                    return loggers.filter(l => {
                        const name: string = l.name.toLowerCase();
                        return filters.every(f => name.includes(f));
                    });
                }
            }),
        );
    }

    private get cleanedFilter$(): Observable<string[]> {
        return this.filter$.pipe(
            debounceTime(200),
            map(f => {
                const clean = f.toLowerCase().trim();
                if (clean) {
                    return clean.split(/ +/);
                } else {
                    return [] as string[];
                }
            }),
            distinctUntilChanged((a, b) => a.join(' ') === b.join(' ')),
        );
    }

    private sortLoggers(loggers$: Observable<Log[]>): Observable<Log[]> {
        return combineLatest([
            loggers$,
            this.sortOrder$,
        ]).pipe(
            map(([loggers, sortOrder]) => sortOrder.sort(loggers)),
        );
    }

    private pageLoggers(loggers$: Observable<Log[]>): Observable<Log[]> {
        return combineLatest([
            loggers$,
            this.page$,
        ]).pipe(
            map(([loggers, page]) => loggers.slice(0, (page + 1) * this.itemsPerPage)),
        );
    }

    loadMore() {
        this.page$.next(this.page$.value + 1);
    }

    ngOnInit() {
        this.subscriptions.add(this.logsService.findAll().subscribe(
            (loggers) => this._loggers$.next(loggers)),
        );
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
        this._loggers$.complete();
        this.filter$.complete();
        this.page$.complete();
        this._sortOrder$.complete();
    }

    sort(sortOrder: SortOrder) {
        this.page$.next(0);
        this._sortOrder$.next(sortOrder);
    }

    changeFilter(filter: string) {
        this.page$.next(0);
        this.filter$.next(filter);
    }

    changeLevel(name: string, level: string) {
        const log = new Log(name, level);
        this.subscriptions.add(this.logsService.changeLevel(log).pipe(
            switchMap(() => this.logsService.findAll()),
        ).subscribe(
            (loggers) => this._loggers$.next(loggers),
        ));
    }

    trackByLoggerName(index: number, logger: Log): string {
        return logger.name;
    }
}
