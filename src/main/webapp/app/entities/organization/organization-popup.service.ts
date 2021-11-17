import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { Organization, OrganizationService } from '../../shared';

@Injectable({ providedIn: 'root' })
export class OrganizationPopupService {
    private isOpen = false;

    constructor(
            private datePipe: DatePipe,
            private modalService: NgbModal,
            private router: Router,
            private organizationService: OrganizationService,
    ) {
    }

    open(component: any, organizationName?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (organizationName) {
            this.organizationService.find(organizationName).subscribe((organization) => {
                // organization.startDate = this.datePipe
                // .transform(organization.startDate, 'yyyy-MM-ddThh:mm');
                // organization.endDate = this.datePipe
                // .transform(organization.endDate, 'yyyy-MM-ddThh:mm');
                this.organizationModalRef(component, organization);
            });
        } else {
            return this.organizationModalRef(component, new Organization());
        }
    }

    organizationModalRef(component: any, organization: Organization): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.organization = organization;
        modalRef.result.then((result) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
