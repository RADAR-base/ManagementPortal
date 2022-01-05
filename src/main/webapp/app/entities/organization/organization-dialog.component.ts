import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {
    NgbActiveModal,
    NgbModalRef,
} from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceTypeService } from '../source-type';
import { OrganizationPopupService } from './organization-popup.service';

import { Organization, OrganizationService } from '../../shared';

@Component({
    selector: 'jhi-organization-dialog',
    templateUrl: './organization-dialog.component.html',
    styleUrls: ['organization-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class OrganizationDialogComponent implements OnInit {
    readonly authorities: any[];
    readonly options: string[];

    organizationCopy: Organization;
    organization: Organization;
    isSaving: boolean;
    organizationIdAsPrettyValue: boolean;

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private organizationService: OrganizationService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.options = ['Work-package', 'Phase', 'External-organization-url', 'External-organization-id', 'Privacy-policy-url'];
        this.organizationIdAsPrettyValue = true;
    }

    ngOnInit() {
        this.organizationCopy = Object.assign({}, this.organization);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.organizationCopy.id !== undefined) {
            this.organizationService.update(this.organizationCopy)
            .subscribe((res: Organization) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.organizationService.create(this.organizationCopy)
            .subscribe((res: Organization) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Organization) {
        this.eventManager.broadcast({name: 'organizationListModification', content: 'OK'});
        this.isSaving = false;
        this.organization = this.organizationCopy;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}

@Component({
    selector: 'jhi-organization-popup',
    template: '',
})
export class OrganizationPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private organizationPopupService: OrganizationPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.organizationPopupService
                    .open(OrganizationDialogComponent, params['organizationName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
