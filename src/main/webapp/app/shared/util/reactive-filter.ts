/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { BehaviorSubject, merge, Observable, Subject } from "rxjs";
import { debounceTime, distinctUntilChanged, filter, map, shareReplay } from "rxjs/operators";
import {
  NgbCalendar,
  NgbDate,
  NgbDateParserFormatter,
  NgbDateStruct
} from "@ng-bootstrap/ng-bootstrap";

export class ReactiveFilter<T> {
  private readonly _value$: BehaviorSubject<T>;
  private readonly _error$: BehaviorSubject<string>;
  private readonly trigger$: Subject<T>;

  error$: Observable<string>
  rawValue$: Observable<T | null>
  value$: Observable<T | null>;
  formattedValue$: Observable<string>;

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
    let debouncedValue = this._value$.pipe(debounceTime(300));
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
    if (options.mapValue) {
      debouncedValue = options.mapValue(debouncedValue);
    }
    let mergedSignal = merge(debouncedValue, this.trigger$.pipe(debounceTime(10)));
    if (options.mapResult) {
      mergedSignal = options.mapResult(mergedSignal);
    } else {
      mergedSignal = mergedSignal.pipe(distinctUntilChanged());
    }
    this.value$ = mergedSignal.pipe(shareReplay(1));
    if (options.formatResult) {
      this.formattedValue$ = options.formatResult(this.value$);
    } else {
      this.formattedValue$ = this.value$.pipe(
        map(v => v !== null ? v.toString() : '')
      );
    }
  }

  get rawValue(): T | null {
    return this._value$.value;
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
  validate?: (value: T | null) => string | null;
  mapValue?: (value$: Observable<T | null>) => Observable<T | null>
  mapResult?: (value$: Observable<T | null>) => Observable<T | null>,
  formatResult?: (value$: Observable<T | null>) => Observable<string>
}

export class NgbDateReactiveFilter extends ReactiveFilter<NgbDateStruct> {
  constructor(
    calendar: NgbCalendar,
    formatter: NgbDateParserFormatter,
    options: ReactiveFilterOptions<NgbDateStruct> = {},
  ) {
    super({
      validate: options.validate,
      mapValue: options.mapValue ? options.mapValue : $v => $v.pipe(
        filter(date => date === null || calendar.isValid(NgbDate.from(date)))
      ),
      mapResult: options.mapResult ? options.mapResult : $v => $v.pipe(
        distinctUntilChanged((d1, d2) => d1 === d2
          || (d1 !== null && NgbDate.from(d1).equals(d2)))
      ),
      formatResult: options.formatResult ? options.formatResult : $v => $v.pipe(
        map(v => formatter.format(v))
      ),
    });
  }
}
