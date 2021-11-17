import { Injectable } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivate,
    CanActivateChild,
    Router,
    RouterStateSnapshot
} from '@angular/router';

import { AuthService } from '../';
import { StateStorageService } from './state-storage.service';
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class UserRouteAccessService implements CanActivate, CanActivateChild {

    constructor(private router: Router,
                private auth: AuthService,
                private stateStorageService: StateStorageService) {
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        this.setStateStorage(route);
        return this.auth.authorize();
    }

    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this.canActivate(route, state);
    }

    setStateStorage(route: ActivatedRouteSnapshot) {
        if (route !== undefined) {
            this.stateStorageService.storeDestinationState({
                path: route.url,
                queryParams: route.queryParams,
                authorities: route.data?.authorities,
            });
        } else {
            this.stateStorageService.resetDestinationState();
        }
    }
}
