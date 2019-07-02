import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, JhiLanguageService } from 'ng-jhipster';
import { OAuthClient, OAuthClientService } from '../../entities/oauth-client';
import { OAuthClientPairInfoService } from '../../entities/oauth-client/oauth-client-pair-info.service';

import { SubjectPopupService } from './subject-popup.service';
import { Subject } from './subject.model';
import { DatePipe } from '@angular/common';
import { DOCUMENT } from '@angular/common';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-subject-pair-dialog',
    templateUrl: './subject-pair-dialog.component.html',
    styleUrls: ['./subject-pair-dialog.component.scss'],
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
                private oauthClientPairInfoService: OAuthClientPairInfoService,
                private datePipe: DatePipe,
                @Inject(DOCUMENT) private doc) {
        this.jhiLanguageService.addLocation('subject');
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
    }

    ngOnInit() {
        this.loadInconsolataFont();
        this.oauthClientService.query().subscribe(
                (res: HttpResponse<any>) => {
                    // only keep clients that have the dynamic_registration key in additionalInformation
                    // and have set it to true
                    this.oauthClients = res.body
                    .filter((c) => c.additionalInformation.dynamic_registration &&
                            c.additionalInformation.dynamic_registration.toLowerCase() === 'true');
                });
    }

    private loadInconsolataFont() {
        if (this.doc.getElementById('inconsolata-font-link') === null) {
            const link: HTMLLinkElement = this.doc.createElement('link');
            link.id = 'inconsolata-font-link';
            link.setAttribute('rel', 'stylesheet');
            link.setAttribute('type', 'text/css');
            link.setAttribute('href', '//fonts.googleapis.com/css?family=Inconsolata');
            this.doc.head.appendChild(link);
        }
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
                    (res: HttpResponse<any>) => {
                        this.oauthClientPairInfo = res.body;
                        this.showQRCode = true;
                        const timesOutAt  = this.datePipe
                                .transform(this.oauthClientPairInfo.timesOutAt, 'dd/MM/yy HH:mm');
                        this.alertService.info('managementPortalApp.subject.tokenTimeoutMessage',
                                {at: timesOutAt, time: this.oauthClientPairInfo.timeout}, null);
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
