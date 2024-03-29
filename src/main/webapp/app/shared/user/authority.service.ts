import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Principal } from "../auth/principal.service";
import {
  concatMap,
  delay,
  distinctUntilChanged,
  map,
  retryWhen,
  shareReplay,
  switchMap,
  take
} from "rxjs/operators";
import { Authority } from "./authority.model";

@Injectable({ providedIn: 'root' })
export class AuthorityService {
  private resourceUrl = 'api/authorities';

  authorities$: Observable<Authority[]>

  constructor(
    private http: HttpClient,
    private principal: Principal,
  ) {
    this.authorities$ = this.principal.account$.pipe(
      map(account => !!account && account.authorities.includes("ROLE_SYS_ADMIN")),
      distinctUntilChanged(),
      switchMap(account => {
        if (account) {
          return this.findAll();
        } else {
          return of([]);
        }
      }),
      retryWhen(errors => errors.pipe(
        delay(1000),
        take(10),
        concatMap(() => of([])))
      ),
      shareReplay(1),
    );
  }

  findAll(): Observable<Authority[]> {
    return this.http.get<Authority[]>(this.resourceUrl);
  }
}
