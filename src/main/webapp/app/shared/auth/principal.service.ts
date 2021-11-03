import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, Subject } from 'rxjs';

import { AccountService } from './account.service';
import { catchError, map } from "rxjs/operators";

@Injectable({ providedIn: 'root' })
export class Principal {
    private userIdentity: any;
    private authenticated = false;
    private authenticationState = new BehaviorSubject<any>(null);

    constructor(
        private account: AccountService
    ) {}

    authenticate(identity) {
        this.userIdentity = identity;
        this.authenticated = identity !== null;
        this.authenticationState.next(this.userIdentity);
    }

    hasAnyAuthority(authorities: string[]): Promise<boolean> {
        if (!this.authenticated || !this.userIdentity || !this.userIdentity.authorities) {
            return Promise.resolve(false);
        }

        for (let i = 0; i < authorities.length; i++) {
            if (this.userIdentity.authorities.indexOf(authorities[i]) !== -1) {
                return Promise.resolve(true);
            }
        }

        return Promise.resolve(false);
    }

    identity(force?: boolean): Promise<any> {
        // check and see if we have retrieved the userIdentity data from the server.
        // if we have, reuse it by immediately resolving
        if (!force && this.userIdentity) {
            return Promise.resolve(this.userIdentity);
        }

        // retrieve the userIdentity data from the server, update the identity object, and then resolve.
        return this.account.get()
            .pipe(
              catchError(() => of()),
              map((account) => {
                  if (account) {
                      this.userIdentity = account;
                      this.authenticated = true;
                  } else {
                      this.userIdentity = null;
                      this.authenticated = false;
                  }
                  this.authenticationState.next(this.userIdentity);
                  return this.userIdentity;
              }),
            )
            .toPromise();
    }

    isAuthenticated(): boolean {
        return this.authenticated;
    }

    isIdentityResolved(): boolean {
        return this.userIdentity !== undefined;
    }

    getAuthenticationState(): Observable<any> {
        return this.authenticationState.asObservable();
    }
}
