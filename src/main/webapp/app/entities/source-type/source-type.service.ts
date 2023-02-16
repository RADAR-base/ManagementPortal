import { Injectable } from '@angular/core';
import { combineLatest, empty, Observable, of, Subject, throwError } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { createRequestOption } from '../../shared/model/request.utils';
import { SourceType } from './source-type.model';
import {
  catchError, concatMap, delay,
  distinct,
  distinctUntilChanged,
  map, pluck,
  publishReplay,
  refCount, retryWhen, shareReplay,
  startWith,
  switchMap, take, tap
} from "rxjs/operators";
import { Principal } from "../../shared";
import { Source } from "../../shared/source";
import { AlertService } from "../../shared/util/alert.service";

@Injectable({ providedIn: 'root' })
export class SourceTypeService {

    private resourceUrl = 'api/source-types';

    private trigger$ = new Subject<void>();
    sourceTypes$: Observable<SourceType[]>

    constructor(
      private http: HttpClient,
      private principal: Principal,
      private alertService: AlertService,
    ) {
        this.sourceTypes$ = combineLatest([
          principal.account$,
          this.trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
          switchMap(([account]) => account
            ? this.query().pipe(
              pluck('body'),
            ) : of<SourceType[]>([])),
          retryWhen(errors => errors.pipe(
            delay(1000),
            take(10),
            concatMap(err => {
              this.alertService.error(err.message || err.toString(), null, null);
              return of([]);
            }))
          ),
          shareReplay(1),
        );
    }

    create(sourceType: SourceType): Observable<SourceType> {
      const copy: SourceType = Object.assign({}, sourceType);
      return this.http.post<SourceType>(this.resourceUrl, copy).pipe(
        tap(() => this.trigger$.next()),
      );
    }

    update(sourceType: SourceType): Observable<SourceType> {
      const copy: SourceType = Object.assign({}, sourceType);
      return this.http.put<SourceType>(this.resourceUrl, copy).pipe(
        tap(() => this.trigger$.next()),
      );
    }

    find(producer: string, model: string, version: string): Observable<SourceType> {
        return this.sourceTypes$.pipe(
          map(sourceTypes => {
              if (sourceTypes) {
                const type = sourceTypes.find(t =>
                  t.producer === producer
                  && t.model === model
                  && t.catalogVersion === version)
                if (!type) {
                  throw new Error(`No source type found with spec ${producer}, ${model}, ${version}`);
                }
                return type;
              } else {
                return null;
              }
          }),
        );
    }

    query(req?: any): Observable<HttpResponse<SourceType[]>> {
        const params = createRequestOption(req);
        return this.http.get<SourceType[]>(this.resourceUrl, {params, observe: 'response' });
    }

    delete(producer: string, model: string, version: string): Observable<object> {
      return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(producer)}/${encodeURIComponent(model)}/${encodeURIComponent(version)}`).pipe(
        tap(() => this.trigger$.next()),
      );
    }
}
