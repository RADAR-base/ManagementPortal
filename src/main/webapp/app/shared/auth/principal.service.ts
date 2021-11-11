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
        private account: AccountService
    ) {
        // initial fetch
        this.account.get().pipe(
          filter(a => !!a)
        ).subscribe(account => this._account$.next(account));
    }

    authenticate(identity?: Account) {
        this._account$.next(identity ? identity : null);
    }

    userHasAnyAuthority(userIdentity: any, authorities: string[]): boolean {
        if (!userIdentity || !userIdentity.authorities) {
            return false;
        }
        for (let i = 0; i < authorities.length; i++) {
            if (userIdentity.authorities.indexOf(authorities[i]) !== -1) {
                return true;
            }
        }
        return false;
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
          })
        );
    }
}
