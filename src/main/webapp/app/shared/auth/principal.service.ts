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

    /**
     * Update authentication state. If new authentication state is null, the user is from the
     * frontend perspective logged out.
     */
    authenticate(identity?: Account) {
        this._account$.next(identity ? identity : null);
    }

    /**
     * Whether user has any of the required authorities.
     * @param account account to check.
     * @param authorities authorities to check. If empty, this method always returns true.
     */
    accountHasAnyAuthority(account: any, authorities: string[] | null): boolean {
        if (!authorities || authorities.length === 0) {
            return true;
        }
        if (!account || !account.authorities) {
            return false;
        }
        return account.authorities.some(a => authorities.indexOf(a) !== -1);
    }

    /**
     * Reset authentication state based on the current authentication state in the server.
     */
    reset(): Observable<Account | null> {
        return this.account.get().pipe(
          catchError(() => of(null)),
          tap(account => this._account$.next(account ? account : null))
        );
    }

    /**
     * Returns the single latest identity. If the user is not logged in, login status will be
     * checked with the server.
     */
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
