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

    setStateStorage(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        let params = {};
        let destinationData = {};
        let destinationName = '';
        const destinationEvent = route;
        if (destinationEvent !== undefined) {
            params = destinationEvent.params;
            destinationData = destinationEvent.data;
            destinationName = state.url;
        }
        const from = {name: this.router.url.slice(1)};
        const destination = {name: destinationName, data: destinationData};
        this.stateStorageService.storeDestinationState(destination, params, from);
    }
}
