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
                this.organizationModalRef(component, organization);
            });
        } else {
            return this.organizationModalRef(component, new Organization());
        }
    }

    organizationModalRef(component: any, organization: Organization): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.organization = organization;
        modalRef.result.then(() => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, () => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
