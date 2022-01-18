import { Component, OnDestroy, ViewEncapsulation } from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';

import { NgbActiveModal, NgbModalRef, } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceTypeService } from '../source-type';
import { OrganizationPopupService } from './organization-popup.service';

import { copyOrganization, Organization, OrganizationService } from '../../shared';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-organization-dialog',
    templateUrl: './organization-dialog.component.html',
    styleUrls: ['organization-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class OrganizationDialogComponent implements OnDestroy {
    readonly authorities: any[];
    readonly options: string[];

    organization: Organization;
    isSaving: boolean;
    organizationIdAsPrettyValue: boolean;

    private subscriptions: Subscription = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private organizationService: OrganizationService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
            private router: Router,
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.options = ['Work-package', 'Phase', 'External-organization-url', 'External-organization-id', 'Privacy-policy-url'];
        this.organizationIdAsPrettyValue = true;
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss();
    }

    save() {
        this.isSaving = true;
        if (this.organization.id !== undefined) {
            this.subscriptions.add(this.organizationService.update(this.organization).subscribe(
                (res: Organization) => this.onSaveSuccess(res),
                () => this.onSaveError(),
            ));
        } else {
            this.subscriptions.add(this.organizationService.create(this.organization).subscribe(
                (res: Organization) => this.onSaveSuccess(res),
                () => this.onSaveError(),
            ));
        }
    }

    private onSaveSuccess(result: Organization) {
        if(history.state?.parentComponent === 'organization-detail') {
            this.router.navigate(['/organization', result.name]);
        }
        this.eventManager.broadcast({name: 'organizationListModification', content: 'OK'});
        this.isSaving = false;
        this.organization = copyOrganization(result);
        this.activeModal.dismiss(result);
    }

    private onSaveError() {
        this.isSaving = false;
    }
}

@Component({
    selector: 'jhi-organization-popup',
    template: '',
})
export class OrganizationPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private organizationPopupService: OrganizationPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.organizationPopupService.open(OrganizationDialogComponent, params['organizationName']);
    }
}
