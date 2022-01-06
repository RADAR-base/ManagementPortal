import { Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { OrganizationPopupService } from './organization-popup.service';

import { Organization, OrganizationService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable } from 'rxjs';

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
export class OrganizationDeletePopupComponent extends ObservablePopupComponent {
    constructor(
            route: ActivatedRoute,
            private organizationPopupService: OrganizationPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.organizationPopupService.open(OrganizationDeleteDialogComponent, params['organizationName']);
    }
}
