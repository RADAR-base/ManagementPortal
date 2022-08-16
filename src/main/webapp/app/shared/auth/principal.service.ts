import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';

import { AccountService } from './account.service';
import { catchError, distinctUntilChanged, first, mergeMap, tap } from 'rxjs/operators';
import { Account } from '../user/account.model';

@Injectable({ providedIn: 'root' })
export class Principal {
    private _account$ = new BehaviorSubject<Account | null>(null);
    readonly account$: Observable<Account | null>;

    constructor(
        private account: AccountService,
    ) {
        this.account$ = this._account$.asObservable().pipe(
            // do not emit multiple duplicate values
            distinctUntilChanged((a, b) => a === b),
        );
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
        if (!account || !account.roles) {
            return false;
        }
        const authoritySet: Set<string> = new Set(authorities);

        return account.roles.some(r =>
          authoritySet.has(r.authorityName)
          || (r.projectName && authoritySet.has(r.authorityName + ':' + r.projectName))
          || (r.organizationName && authoritySet.has(r.authorityName + ':' + r.organizationName))
        )
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
