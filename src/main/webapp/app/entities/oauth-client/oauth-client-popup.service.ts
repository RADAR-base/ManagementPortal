import { DatePipe } from '@angular/common';
import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';

@Injectable()
export class OAuthClientPopupService {
    private isOpen = false;

    constructor(
            private datePipe: DatePipe,
            private modalService: NgbModal,
            private router: Router,
            private oauthClientService: OAuthClientService,
    ) {
    }

    open(component: any, clientId?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (clientId) {
            this.oauthClientService.find(clientId).subscribe((client) => {
                this.oauthClientModalRef(component, client);
            });
        } else {
            return this.oauthClientModalRef(component, new OAuthClient());
        }
    }

    oauthClientModalRef(component: any, client: OAuthClient): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.client = client;
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
