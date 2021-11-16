import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, Subject } from 'rxjs';

import { AccountService } from './account.service';
import { catchError, filter, first, map, mergeMap, switchMap, tap } from "rxjs/operators";
import { Account } from "../user/account.model";

@Injectable({ providedIn: 'root' })
export class Principal {
    private _account$ = new BehaviorSubject<Account | null>(null);
    readonly account$: Observable<Account | null> = this._account$.asObservable();

    constructor(
        private account: AccountService,
    ) {
        this.reset().subscribe();
    }

    authenticate(identity?: Account) {
        this._account$.next(identity ? identity : null);
    }

    userHasAnyAuthority(userIdentity: any, authorities: string[] | null): boolean {
        if (!authorities || authorities.length === 0) {
            return true;
        }
        if (!userIdentity || !userIdentity.authorities) {
            return false;
        }
        return userIdentity.authorities.some(a => authorities.indexOf(a) !== -1);
    }

    reset(): Observable<Account | null> {
        return this.account.get().pipe(
          catchError(() => of(null)),
          tap(account => this._account$.next(account ? account : null))
        );
    }

    identity(): Observable<Account | null> {
        // check and see if we have retrieved the userIdentity data from the server.
        // if we have, reuse it by immediately resolving
        return this._account$.pipe(
          first(),
          mergeMap((user?: Account) => {
              return user ? of(user) : this.reset();
          }),
        );
    }
}
