import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest, Observable, Subject, Subscription } from 'rxjs';
import { Organization, OrganizationService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import {
    distinctUntilChanged, filter,
    map, shareReplay,
    startWith,
    switchMap
} from "rxjs/operators";

@Component({
    selector: 'jhi-organization-detail',
    templateUrl: './organization-detail.component.html',
    styleUrls: ['organization-detail.component.scss'],
})
export class OrganizationDetailComponent implements OnInit, OnDestroy {
    private trigger$ = new Subject<void>();
    organization$: Observable<Organization>;
    private eventSubscriber: Subscription;

    showProjects: boolean;
    showPermissions: boolean;

    constructor(
            private eventManager: EventManager,
            private organizationService: OrganizationService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.organization$ = combineLatest([
          this.route.params,
          this.trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
          map(([params]) => params['organizationName']),
          filter(orgName => !!orgName),  // ensure that organization name is set
          distinctUntilChanged(),  // no need to trigger duplicate requests
          switchMap((orgName) => this.organizationService.find(orgName)), // get organization
          shareReplay(1), // multiple subscriptions will not trigger multiple requests
        );
        this.eventSubscriber = this.registerChangeInOrganizations();
        this.viewProjects();
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.trigger$.complete();
        this.eventSubscriber.unsubscribe();
    }

    registerChangeInOrganizations(): Subscription {
        return this.eventManager.subscribe('organizationListModification',
          () => this.trigger$.next());
    }

    viewProjects() {
        this.showProjects = true;
        this.showPermissions = false;
    }

    viewPermissions() {
        this.showProjects = false;
        this.showPermissions = true;
    }
}
