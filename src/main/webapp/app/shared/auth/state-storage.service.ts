import { Injectable } from '@angular/core';
import { SessionStorageService } from 'ngx-webstorage';
import { Params, Router } from "@angular/router";

export interface RouteStateSegment {
    path: string;
    /** The matrix parameters associated with a segment */
    parameters: Params;
}

export interface RouteState {
    path?: RouteStateSegment[];
    queryParams?: Params;
    authorities?: string[];
}

@Injectable({ providedIn: 'root' })
export class StateStorageService {
    constructor(
      private $sessionStorage: SessionStorageService,
      private router: Router,
    ) {}

    getPreviousState(): RouteState | null {
        return this.$sessionStorage.retrieve('previousState');
    }

    resetPreviousState() {
        this.$sessionStorage.clear('previousState');
    }

    storePreviousState(state: RouteState) {
        this.$sessionStorage.store('previousState', state);
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
