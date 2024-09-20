import {DatePipe} from '@angular/common';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {copyOrganization, Organization, OrganizationService} from '../../shared';
import {Observable, of} from 'rxjs';
import {first, map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class OrganizationPopupService {
    private isOpen = false;

    constructor(
        private datePipe: DatePipe,
        private modalService: NgbModal,
        private router: Router,
        private organizationService: OrganizationService,
    ) {
    }

    open(component: any, organizationName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (organizationName) {
            return this.organizationService.find(organizationName).pipe(
                first(),
                map((organization) => this.organizationModalRef(component, organization)),
            )
        } else {
            return of(this.organizationModalRef(component, {}));
        }
    }

    organizationModalRef(component: any, organization: Organization): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.organization = copyOrganization(organization);
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
