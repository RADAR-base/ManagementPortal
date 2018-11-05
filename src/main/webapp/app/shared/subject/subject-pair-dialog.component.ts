import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, JhiLanguageService } from 'ng-jhipster';
import { OAuthClient, OAuthClientService } from '../../entities/oauth-client';
import { OAuthClientPairInfoService } from '../../entities/oauth-client/oauth-client-pair-info.service';

import { SubjectPopupService } from './subject-popup.service';
import { Subject } from './subject.model';

@Component({
    selector: 'jhi-subject-pair-dialog',
    templateUrl: './subject-pair-dialog.component.html',
    providers: [OAuthClientService, OAuthClientPairInfoService],
})
export class SubjectPairDialogComponent implements OnInit {
    readonly authorities: string[];

    subject: Subject;
    oauthClients: OAuthClient[];
    oauthClientPairInfo: any;
    selectedClient: OAuthClient;
    showQRCode = false;
    showTokenUrl = false;

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private oauthClientService: OAuthClientService,
                private oauthClientPairInfoService: OAuthClientPairInfoService) {
        this.jhiLanguageService.addLocation('subject');
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
    }

    ngOnInit() {
        this.oauthClientService.query().subscribe(
                (res) => {
                    // only keep clients that have the dynamic_registration key in additionalInformation
                    // and have set it to true
                    this.oauthClients = res.json()
                    .filter((c) => c.additionalInformation.dynamic_registration &&
                            c.additionalInformation.dynamic_registration.toLowerCase() === 'true');
                });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    trackOAuthClientById(index: number, item: OAuthClient) {
        return item.clientId;
    }

    updateQRCode() {
        if (this.selectedClient !== null) {
            this.oauthClientPairInfoService.get(this.selectedClient, this.subject).subscribe(
                    (res) => {
                        this.oauthClientPairInfo = res.json();
                        this.showQRCode = true;
                    });
        } else {
            this.showQRCode = false;
            this.oauthClientPairInfo = {};
        }
    }

    unlockTokenUrl() {
        this.showTokenUrl = true;
    }

    lockTokenUrl() {
        this.showTokenUrl = false;
    }

}

@Component({
    selector: 'jhi-subject-pair-popup',
    template: '',
})
export class SubjectPairPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.subjectPopupService
                    .open(SubjectPairDialogComponent, params['login']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
