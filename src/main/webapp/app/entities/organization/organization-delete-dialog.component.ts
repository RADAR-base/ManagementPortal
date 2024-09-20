import {Component} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {OrganizationPopupService} from './organization-popup.service';

import {Organization, OrganizationService} from '../../shared';
import {ObservablePopupComponent} from '../../shared/util/observable-popup.component';
import {Observable} from 'rxjs';

@Component({
    selector: 'jhi-organization-delete-dialog',
    templateUrl: './organization-delete-dialog.component.html',
})
export class OrganizationDeleteDialogComponent {

    organization: Organization;

    constructor(
        private organizationService: OrganizationService,
        public activeModal: NgbActiveModal,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(organizationName: string) {
        this.organizationService.delete(organizationName).subscribe(() => {
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
