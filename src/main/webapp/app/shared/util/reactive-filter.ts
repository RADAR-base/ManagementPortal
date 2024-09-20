/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {BehaviorSubject, interval, Observable, of, Subject} from "rxjs";
import {debounce, distinctUntilChanged, filter, map, shareReplay, startWith,} from "rxjs/operators";
import {NgbCalendar, NgbDate, NgbDateParserFormatter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

export class ReactiveFilter<T> {
    error$: Observable<string>;
    value$: Observable<T | null>;
    protected _value?: T;
    protected readonly debounceTime: number
    protected readonly _error$: BehaviorSubject<string>;
    protected readonly trigger$: Subject<number>;

    constructor(
        options?: ReactiveFilterOptions<T>
    ) {
        if (!options) {
            options = {};
        }
        this._error$ = new BehaviorSubject('');
        this.error$ = this._error$.asObservable().pipe(distinctUntilChanged());

        this.debounceTime = typeof options.debounceTime === 'number' ? options.debounceTime : 200;
        this._value = null;
        this.trigger$ = new Subject();

        let signal = this.trigger$.pipe(
            startWith(0),
            debounce(t => t ? interval(t) : of()),
            map(() => this._value),
            filter(value => {
                const error = this.validate(value);
                if (error) {
                    this._error$.next(error);
                    return false;
                } else {
                    this._error$.next('');
                    return true;
                }
            }),
        );
        if (options.mapResult) {
            signal = options.mapResult(signal);
        } else {
            signal = signal.pipe(distinctUntilChanged());
        }
        this.value$ = signal.pipe(shareReplay(1));
    }

    next(value?: T, immediately?: boolean) {
        this._value = value;
        if (immediately) {
            this.trigger$.next(0);
        } else {
            this.trigger$.next(this.debounceTime);
        }
    }

    clear() {
        this._value = null;
        this._error$.next('');
        this.trigger$.next(0);
    }

    complete() {
        this._value = undefined;
        this._error$.complete();
        this.trigger$.complete();
    }

    validate(value: T | null): string {
        return '';
    }
}

export interface ReactiveFilterOptions<T> {
    debounceTime?: number | null,
    initialValue?: T | null;
    mapResult?: (value$: Observable<T | null>) => Observable<T | null>;
}

export class NgbDateReactiveFilter extends ReactiveFilter<NgbDateStruct> {
    constructor(
        private calendar: NgbCalendar,
        private dateFormatter: NgbDateParserFormatter,
        options: ReactiveFilterOptions<NgbDateStruct> = {},
    ) {
        super({
            mapResult: options.mapResult ? options.mapResult : $v => $v.pipe(
                distinctUntilChanged((d1, d2) => d1 === d2
                    || (d1 && NgbDate.from(d1).equals(d2)))
            ),
            debounceTime: 0,
            initialValue: options.initialValue,
        });
    }

    public static isValidRange(from?: NgbDateStruct, to?: NgbDateStruct): boolean {
        if (from && to) {
            const dateFrom = NgbDate.from(from);
            const dateTo = NgbDate.from(to)
            if (dateFrom.after(dateTo)) {
                return false;
            }
        }
        return true;
    }

    validate(date: NgbDateStruct | null): string {
        if (date === null) {
            return '';
        }
        if (!this.calendar.isValid(NgbDate.from(date))) {
            return 'invalidDate';
        }
        return '';
    }

    next(value?: NgbDateStruct | string) {
        if (typeof value === 'string') {
            super.next(this.dateFormatter.parse(value));
        } else {
            super.next(value);
        }
    }
}

export interface NgbDateRange {
    from: NgbDateStruct;
    to: NgbDateStruct;
}
