import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { LoginModalService } from '../login/login-modal.service';
import { Principal } from './principal.service';
import { StateStorageService } from './state-storage.service';
import { Observable } from "rxjs";
import { map } from "rxjs/operators";

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
                const authorities = destState.data.authorities;
                const hasAnyAuthority = this.principal.userHasAnyAuthority(user, authorities);
                if (!hasAnyAuthority) {
                    if (user) {
                        // user is signed in but not authorized for desired state
                        this.router.navigate(['accessdenied']);
                    } else {
                      this.handleUnauthorized();
                    }
                }
                return hasAnyAuthority;
            }),
        );
    }

    resetAuthentication() {
      this.principal.authenticate(null);
      this.handleUnauthorized();
    }

    private handleUnauthorized() {
      const destState = this.stateStorageService.getDestinationState();
      // user is not authenticated. Show the state they wanted before you
      // send them to the login service, so you can return them when you're done
      this.stateStorageService.storePreviousState(destState);
      // now, send them to the signin state so they can log in
      this.router.navigate(['accessdenied']);
    }
}
