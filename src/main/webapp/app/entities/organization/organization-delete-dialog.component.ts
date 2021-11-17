import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { OrganizationPopupService } from './organization-popup.service';

import { Organization, OrganizationService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

@Component({
    selector: 'jhi-organization-delete-dialog',
    templateUrl: './organization-delete-dialog.component.html',
})
export class OrganizationDeleteDialogComponent {

    organization: Organization;

    constructor(
            private organizationService: OrganizationService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(organizationName: string) {
        this.organizationService.delete(organizationName).subscribe(() => {
            this.eventManager.broadcast({
                name: 'organizationListModification',
                content: 'Deleted an organization',
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-organization-delete-popup',
    template: '',
})
export class OrganizationDeletePopupComponent implements OnInit, OnDestroy {

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
                    .open(OrganizationDeleteDialogComponent, params['organizationName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
