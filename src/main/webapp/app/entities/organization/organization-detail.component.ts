import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest, Observable, Subject, Subscription } from 'rxjs';
import { Organization, OrganizationService, Principal } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import {
    distinctUntilChanged,
    filter,
    map,
    shareReplay,
    startWith,
    switchMap
} from 'rxjs/operators';

@Component({
    selector: 'jhi-organization-detail',
    templateUrl: './organization-detail.component.html',
    styleUrls: ['organization-detail.component.scss'],
})
export class OrganizationDetailComponent implements OnInit, OnDestroy {
    private trigger$ = new Subject<void>();
    organization$: Observable<Organization>;
    userRoles$: Observable<{ organizationAdmin: boolean }>
    private eventSubscriber: Subscription;

    showProjects: boolean;
    showPermissions: boolean;

    constructor(
            private eventManager: EventManager,
            private organizationService: OrganizationService,
            private route: ActivatedRoute,
            public principal: Principal,
    ) {
        this.organization$ = this.observeOrganization();
        this.userRoles$ = this.observeUserRoles(this.organization$);
    }

    ngOnInit() {
        this.eventSubscriber = this.registerChangeInOrganizations();
        this.viewProjects();
    }

    private observeOrganization(): Observable<Organization> {
        return combineLatest([
            this.route.params,
            this.trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
            map(([params]) => params['organizationName']),
            filter(orgName => !!orgName),  // ensure that organization name is set
            distinctUntilChanged(),  // no need to trigger duplicate requests
            switchMap((orgName) => this.organizationService.find(orgName)), // get organization
            shareReplay(1),
        );
    }

    private observeUserRoles(organization$: Observable<Organization>): Observable<{ organizationAdmin: boolean }> {
        return combineLatest([
            this.organization$,
            this.principal.account$,
        ]).pipe(
            map(([organization , account]) => ({
                organizationAdmin: this.principal.accountHasAnyAuthority(account, ['ROLE_SYS_ADMIN', 'ROLE_ORGANIZATION_ADMIN:' + organization.name]),
            })),
        );
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.trigger$.complete();
        this.eventSubscriber.unsubscribe();
    }

    registerChangeInOrganizations(): Subscription {
        return this.eventManager.subscribe('organizationListModification', () => this.trigger$.next());
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
