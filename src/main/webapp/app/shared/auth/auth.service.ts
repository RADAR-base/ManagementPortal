import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { LoginModalService } from '../login/login-modal.service';
import { Principal } from './principal.service';
import { StateStorageService } from './state-storage.service';
import { Observable, of } from "rxjs";
import { debounce, debounceTime, map, switchMap } from "rxjs/operators";
import { Account } from "../user/account.model";

@Injectable({ providedIn: 'root' })
export class AuthService {

    constructor(
            private principal: Principal,
            private stateStorageService: StateStorageService,
            private loginModalService: LoginModalService,
            private router: Router,
    ) {
    }

    authorize(): Observable<boolean> {
        return this.principal.identity().pipe(
            map(user => {
                const destState = this.stateStorageService.getDestinationState();

                // recover and clear previousState after external login redirect (e.g. oauth2)
                const previousState = this.stateStorageService.getPreviousState();
                if (user && !destState.from.name && previousState) {
                    this.stateStorageService.resetPreviousState();
                    this.router.navigate([previousState.name], {queryParams: previousState.params});
                    return false;
                }

                const authorities = destState.destination.data.authorities;
                if (authorities && authorities.length > 0) {
                    const hasAnyAuthority = this.principal.userHasAnyAuthority(user, authorities);
                    if (!hasAnyAuthority) {
                        if (user) {
                            // user is signed in but not authorized for desired state
                            this.router.navigate(['accessdenied']);
                        } else {
                            // user is not authenticated. Show the state they wanted before you
                            // send them to the login service, so you can return them when you're done
                            this.stateStorageService.storePreviousState(destState.destination.name, destState.params);
                            // now, send them to the signin state so they can log in
                            this.router.navigate(['accessdenied']).then(() => {
                                this.loginModalService.open();
                            });
                        }
                    }
                    return hasAnyAuthority;
                }
                return true;
            })
        )
    }
}
