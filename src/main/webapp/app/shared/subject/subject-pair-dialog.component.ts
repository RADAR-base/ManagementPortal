import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { OAuthClient, OAuthClientService } from '../../entities/oauth-client';
import { OAuthClientPairInfoService } from '../../entities/oauth-client/oauth-client-pair-info.service';

import { SubjectPopupService } from './subject-popup.service';
import { Subject } from './subject.model';
import { DatePipe, DOCUMENT } from '@angular/common';
import { TranslateService } from 'ng2-translate';
import { Observable } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-subject-pair-dialog',
    templateUrl: './subject-pair-dialog.component.html',
    styleUrls: ['subject-pair-dialog.component.scss'],
    providers: [OAuthClientService, OAuthClientPairInfoService],
})
export class SubjectPairDialogComponent implements OnInit {
    readonly authorities: string[];

    subject: Subject;
    oauthClients: OAuthClient[];
    pairInfo: any = null;
    selectedClient: OAuthClient = null;
    allowPersistentToken = false;

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private translate: TranslateService,
                private oauthClientService: OAuthClientService,
                private pairInfoService: OAuthClientPairInfoService,
                private datePipe: DatePipe,
                @Inject(DOCUMENT) private doc) {
        this.jhiLanguageService.addLocation('subject');
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
    }

    ngOnInit() {
        if (this.subject.project && this.subject.project.persistentTokenTimeout) {
            this.allowPersistentToken = true;
        }
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

    unsetPairing() {
        this.pairInfo = null;
    }

    generateQRCode(persistent: boolean) {
        if (this.selectedClient !== null) {
            this.pairInfoService.get(this.selectedClient, this.subject, persistent)
                    .subscribe((res: HttpResponse<any>) => {
                        // delete old value
                        if (this.pairInfo != null
                                && this.pairInfo.tokenName != null) {
                            this.pairInfoService.delete(this.pairInfo.tokenName)
                                    .subscribe((deleteRes) => {
                                        if (!deleteRes.ok) {
                                            console.log('Failed to delete stale MetaToken: '
                                                    + JSON.stringify(deleteRes.json()));
                                        }
                                    });
                        }

                        const result = res.body;

                        result.timeOutDate = this.datePipe
                                .transform(result.timesOutAt, 'medium');

                        this.translateTimeout(result.timeout)
                                .subscribe(t => result.timeoutString = t);

                        this.pairInfo = result;
                    });
        } else {
            this.pairInfo = null;
        }
    }

    private translateTimeout(timeout: number): Observable<string> {
        const timeoutMins = timeout / 60000;
        if (timeoutMins < 180 && timeoutMins % 60 !== 0) {
            return this.translate.get(
                    'managementPortalApp.subject.tokenTimeoutMinutes',
                    {minutes: timeoutMins});
        } else {
            const timeoutHours = Math.floor(timeoutMins / 60);

            if (timeoutHours === 1) {
                return this.translate.get(
                        'managementPortalApp.subject.tokenTimeoutHour');
            } else if (timeoutHours <= 48) {
                return this.translate.get(
                        'managementPortalApp.subject.tokenTimeoutHours',
                        {hours: timeoutHours});
            } else {
                return this.translate.get(
                        'managementPortalApp.subject.tokenTimeoutDays',
                        {days: Math.floor(timeoutHours / 24)});
            }
        }
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
