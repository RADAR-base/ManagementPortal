import { Injectable } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { JhiLoginModalComponent } from './login.component';

@Injectable({ providedIn: 'root' })
export class LoginModalService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
    ) {
    }

    open(): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;
        const modalRef = this.modalService.open(JhiLoginModalComponent, {
            container: 'nav',
        });
        modalRef.result.then(() => {
            this.isOpen = false;
        }, () => {
            this.isOpen = false;
        });
        return modalRef;
    }
}
