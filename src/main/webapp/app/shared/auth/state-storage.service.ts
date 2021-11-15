import { Injectable } from '@angular/core';
import { SessionStorageService } from 'ngx-webstorage';
import { Data, Params, RouterState } from "@angular/router";

export interface RouteState {
    path: string;
    data?: Data;
    params: Params;
}

@Injectable({ providedIn: 'root' })
export class StateStorageService {
    constructor(
            private $sessionStorage: SessionStorageService,
    ) {
    }

    getPreviousState(): RouteState | null {
        return this.$sessionStorage.retrieve('previousState');
    }

    resetPreviousState() {
        this.$sessionStorage.clear('previousState');
    }

    storePreviousState(state: RouteState) {
        this.$sessionStorage.store('previousState', state);
    }

    storeUrl(url: string) {
        this.$sessionStorage.store('previousUrl', url);
    }

    getUrl() {
        return this.$sessionStorage.retrieve('previousUrl');
    }

    resetDestinationState() {
        this.$sessionStorage.clear('destinationState');
    }

    storeDestinationState(state: RouteState) {
        this.$sessionStorage.store('destinationState', state);
    }

    getDestinationState(): RouteState | null {
        return this.$sessionStorage.retrieve('destinationState');
    }
}
