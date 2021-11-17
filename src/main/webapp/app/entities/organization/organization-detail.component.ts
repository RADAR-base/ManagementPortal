import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

// import { Source } from '../../shared/source/source.model';
import { Organization, OrganizationService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

@Component({
    selector: 'jhi-organization-detail',
    templateUrl: './organization-detail.component.html',
    styleUrls: ['organization-detail.component.scss'],
})
export class OrganizationDetailComponent implements OnInit, OnDestroy {

    organization: Organization;
    private subscription: any;
    private eventSubscriber: Subscription;

    // sources: Source[];

    showProjects: boolean;
    showPermissions: boolean;
    // showSources: boolean;
    // showSubjects: boolean;
    // showOrganizationGroups: boolean;
    // showSourceTypes: boolean;
    // showOrganizationAdmins: boolean;
    // showOrganizationAnalysts: boolean;

    constructor(
            private eventManager: EventManager,
            private organizationService: OrganizationService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['organizationName']);
        });
        this.registerChangeInOrganizations();
        // this.viewSubjects();
        this.viewProjects();
    }

    load(organizationName) {
        this.organizationService.find(organizationName).subscribe((organization) => {
            this.organization = organization;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInOrganizations() {
        this.eventSubscriber = this.eventManager.subscribe('organizationListModification',
                () => this.load(this.organization.organizationName));
    }

    viewProjects() {
        // this.showSources = false;
        this.showProjects = true;
        this.showPermissions = false;
        // this.showOrganizationGroups = false;
        // this.showSourceTypes = false;
        // this.showOrganizationAdmins = false;
        // this.showOrganizationAnalysts = false;
    }

    viewPermissions() {
        // this.showSources = false;
        this.showProjects = false;
        this.showPermissions = true;
        // this.showOrganizationGroups = false;
        // this.showSourceTypes = false;
        // this.showOrganizationAdmins = false;
        // this.showOrganizationAnalysts = false;
    }

    // viewSources() {
    //     this.showSources = true;
    //     this.showSubjects = false;
    //     this.showOrganizationGroups = false;
    //     this.showSourceTypes = false;
    //     this.showOrganizationAdmins = false;
    //     this.showOrganizationAnalysts = false;
    // }
    //
    // viewSubjects() {
    //     this.showSources = false;
    //     this.showSubjects = true;
    //     this.showOrganizationGroups = false;
    //     this.showSourceTypes = false;
    //     this.showOrganizationAdmins = false;
    //     this.showOrganizationAnalysts = false;
    // }
    //
    // viewOrganizationGroups() {
    //     this.showSources = false;
    //     this.showSubjects = false;
    //     this.showOrganizationGroups = true;
    //     this.showSourceTypes = false;
    //     this.showOrganizationAdmins = false;
    //     this.showOrganizationAnalysts = false;
    // }
    //
    // viewOrganizationAdmins() {
    //     this.showSources = false;
    //     this.showSubjects = false;
    //     this.showOrganizationGroups = false;
    //     this.showSourceTypes = false;
    //     this.showOrganizationAdmins = true;
    //     this.showOrganizationAnalysts = false;
    // }
    //
    // viewOrganizationAnalysts() {
    //     this.showSources = false;
    //     this.showSubjects = false;
    //     this.showOrganizationGroups = false;
    //     this.showSourceTypes = false;
    //     this.showOrganizationAdmins = false;
    //     this.showOrganizationAnalysts = true;
    // }
}
