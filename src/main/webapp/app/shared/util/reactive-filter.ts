/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { BehaviorSubject, merge, Observable, Subject } from "rxjs";
import { debounceTime, distinctUntilChanged, filter, shareReplay, startWith } from "rxjs/operators";
import {
  NgbCalendar,
  NgbDate,
  NgbDateParserFormatter,
  NgbDateStruct
} from "@ng-bootstrap/ng-bootstrap";

export class ReactiveFilter<T> {
  protected readonly _value$: BehaviorSubject<T>;
  protected readonly _error$: BehaviorSubject<string>;
  protected readonly trigger$: Subject<T>;

  error$: Observable<string>
  rawValue$: Observable<T | null>
  value$: Observable<T | null>;

  constructor(
    options?: ReactiveFilterOptions<T>
  ) {
    if (!options) {
      options = {};
    }
    this._error$ = new BehaviorSubject('');
    this.error$ = this._error$.asObservable().pipe(distinctUntilChanged())

    this._value$ = new BehaviorSubject<T>(null);
    this.rawValue$ = this._value$.asObservable();
    this.trigger$ = new Subject<T>();
    let debouncedValue = this._value$.pipe(debounceTime(options.debounceTime || 300));
    if (options.validate) {
      debouncedValue = this._value$.pipe(
        filter(v => {
          const error = options.validate(v);
          if (error) {
            this._error$.next(error);
            return false;
          } else {
            this._error$.next('');
            return true;
          }
        })
      )
    }
    let mergedSignal = merge(debouncedValue, this.trigger$).pipe(
      startWith(options.initialValue || null as T | null),
    );
    if (options.mapResult) {
      mergedSignal = options.mapResult(mergedSignal);
    } else {
      mergedSignal = mergedSignal.pipe(distinctUntilChanged());
    }
    this.value$ = mergedSignal.pipe(shareReplay(1));
  }

  next(value?: T){
    this._value$.next(value);
  }

  clear() {
    this._value$.next(null);
    this._error$.next('');
    this.trigger$.next(null);
  }

  complete() {
    this._value$.complete();
    this._error$.complete();
    this.trigger$.complete();
  }
}

export interface ReactiveFilterOptions<T> {
  debounceTime?: number | null,
  initialValue?: T | null;
  validate?: (value: T | null) => string | null;
  mapResult?: (value$: Observable<T | null>) => Observable<T | null>;
}

export class NgbDateReactiveFilter extends ReactiveFilter<NgbDateStruct> {
  constructor(
    calendar: NgbCalendar,
    private formatter: NgbDateParserFormatter,
    options: ReactiveFilterOptions<NgbDateStruct> = {},
  ) {
    super({
      validate(date) {
          if (date === null) {
            return '';
          }
          if (!calendar.isValid(NgbDate.from(date))) {
            return 'invalidDate';
          }
          if (options.validate) {
            return options.validate(date)
          }
          return '';
      },
      mapResult: options.mapResult ? options.mapResult : $v => $v.pipe(
        distinctUntilChanged((d1, d2) => d1 === d2
          || (d1 && NgbDate.from(d1).equals(d2)))
      ),
      debounceTime: 1,
      initialValue: options.initialValue,
    });
  }

  next(value?: NgbDateStruct | string) {
    if (typeof value === 'string') {
      super.next(this.formatter.parse(value));
    } else {
      super.next(value);
    }
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
}

export interface NgbDateRange {
  from: NgbDateStruct;
  to: NgbDateStruct;
}
