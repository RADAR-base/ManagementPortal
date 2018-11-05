import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

import { AuthService } from '../';
import { StateStorageService } from './state-storage.service';

@Injectable()
export class UserRouteAccessService implements CanActivate {

    constructor(private router: Router,
                private auth: AuthService,
                private stateStorageService: StateStorageService) {
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {

        this.setStateStorage(route, state);
        return this.auth.authorize(false).then(canActivate => {
            return canActivate;
        });
    }

    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {
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
