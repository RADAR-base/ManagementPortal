import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import {Organization, OrganizationService, Principal} from '../../shared';
import {EventManager} from '../../shared/util/event-manager.service';
import {distinctUntilChanged, filter, map, pluck, shareReplay, switchMap} from 'rxjs/operators';

@Component({
    selector: 'jhi-organization-detail',
    templateUrl: './organization-detail.component.html',
    styleUrls: ['organization-detail.component.scss'],
})
export class OrganizationDetailComponent implements OnInit {
    organization$: Observable<Organization>;
    userRoles$: Observable<{ organizationAdmin: boolean }>

    showProjects: boolean;
    showPermissions: boolean;

    constructor(
        private eventManager: EventManager,
        private organizationService: OrganizationService,
        private route: ActivatedRoute,
        public principal: Principal,
    ) {
    }

    ngOnInit() {
        this.organization$ = this.observeOrganization();
        this.userRoles$ = this.observeUserRoles(this.organization$);
        this.viewProjects();
    }

    previousState() {
        window.history.back();
    }

    viewProjects() {
        this.showProjects = true;
        this.showPermissions = false;
    }

    viewPermissions() {
        this.showProjects = false;
        this.showPermissions = true;
    }

    private observeOrganization(): Observable<Organization> {
        return this.route.params.pipe(
            pluck('organizationName'),
            filter(orgName => !!orgName),  // ensure that organization name is set
            distinctUntilChanged(),  // no need to trigger duplicate requests
            switchMap((orgName) => this.organizationService.find(orgName)), // get organization
            shareReplay(1),
        );
    }

    private observeUserRoles(organization$: Observable<Organization>): Observable<{ organizationAdmin: boolean }> {
        return combineLatest([
            organization$,
            this.principal.account$,
        ]).pipe(
            map(([organization, account]) => ({
                organizationAdmin: organization && this.principal.accountHasAnyAuthority(account, ['ROLE_SYS_ADMIN', 'ROLE_ORGANIZATION_ADMIN:' + organization.name]),
            })),
        );
    }
}
