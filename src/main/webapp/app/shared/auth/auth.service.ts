import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { LoginModalService } from '../login/login-modal.service';
import { Principal } from './principal.service';
import { StateStorageService } from './state-storage.service';
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { UserModalService } from '../../admin';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Injectable({ providedIn: 'root' })
export class AuthService {

    constructor(
        private principal: Principal,
        private stateStorageService: StateStorageService,
        private loginModalService: LoginModalService,
        private router: Router,
        private modalService: NgbModal,
    ) {
    }

    authorize(): Observable<boolean> {
        return this.principal.identity().pipe(
            map(account => {
                const destState = this.stateStorageService.getDestinationState();
                const authorities = destState.authorities;
                const hasAnyAuthority = this.principal.accountHasAnyAuthority(account, authorities);
                if (!hasAnyAuthority) {
                    if (account) {
                      // user is signed in but not authorized for desired state
                      this.router.navigate(['accessdenied']);
                    } else {
                      this.handleUnauthenticated();
                    }
                }
                return hasAnyAuthority;
            }),
        );
    }

    redirectBeforeUnauthenticated(): Promise<boolean> {
        const previousState = this.stateStorageService.getPreviousState();
        if (previousState) {
            this.stateStorageService.resetPreviousState();
            return this.router.navigate(
                previousState.path ? previousState.path.map(p => p.path ? p.path : p.parameters) : ['.'],
                { queryParams: previousState.queryParams },
            );
        }
        return Promise.resolve(false);
    }

    /**
     * Resets the authentication status to empty.
     * @param redirect whether to redirect to login screen.
     */
    resetAuthentication(redirect: boolean) {
      this.principal.authenticate(null);
      if (redirect) {
        this.handleUnauthenticated();
      }
    }

    private handleUnauthenticated() {
        // user is not authenticated. Show the state they wanted before you
        // send them to the login service, so you can return them when you're done
      const destState = this.stateStorageService.getDestinationState();
      this.stateStorageService.storePreviousState(destState);

      // now, send them to the sign in state so they can log in
      this.router.navigate(['accessdenied']).then(
          () => this.modalService.dismissAll(),
      );
    }
}
