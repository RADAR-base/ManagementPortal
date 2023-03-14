import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe, DOCUMENT } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { OAuthClient, OAuthClientService, PairInfo } from '../../entities/oauth-client';
import { OAuthClientPairInfoService } from '../../entities/oauth-client/oauth-client-pair-info.service';

import { SubjectPopupService } from './subject-popup.service';
import { Subject } from './subject.model';
import { ObservablePopupComponent } from '../util/observable-popup.component';
import { map, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'jhi-subject-pair-dialog',
    templateUrl: './subject-pair-dialog.component.html',
    styleUrls: ['subject-pair-dialog.component.scss'],
})
export class SubjectPairDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];

    subject: Subject;
    oauthClients: OAuthClient[];
    pairInfo: PairInfo = null;
    selectedClient: OAuthClient = null;
    allowPersistentToken = false;
    private subscriptions: Subscription = new Subscription();

    constructor(public activeModal: NgbActiveModal,
                private translate: TranslateService,
                private oauthClientService: OAuthClientService,
                private pairInfoService: OAuthClientPairInfoService,
                private datePipe: DatePipe,
                @Inject(DOCUMENT) private doc) {
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
    }

    ngOnInit() {
        if (this.subject.project && this.subject.project.persistentTokenTimeout) {
            this.allowPersistentToken = true;
        }
        this.loadInconsolataFont();
        this.subscriptions.add(this.fetchOAuthClients());
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    private fetchOAuthClients(): Subscription {
        return this.oauthClientService.query().subscribe(
            (res: HttpResponse<any>) => {
                // only keep clients that have the dynamic_registration key in additionalInformation
                // and have set it to true
                this.oauthClients = res.body.filter(
                  (c) => c.additionalInformation.dynamic_registration && c.additionalInformation.dynamic_registration.toLowerCase() === 'true'
                );
            }
        );
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
            this.subscriptions.add(this.pairInfoService.get(this.selectedClient, this.subject, persistent).pipe(
                tap(() => {
                    // delete old value
                    if (this.pairInfo && this.pairInfo.tokenName) {
                        this.subscriptions.add(this.deleteToken(this.pairInfo.tokenName));
                    }
                }),
                switchMap(result => this.translateTimeout(result.timeout).pipe(
                    map(t => ({
                        ...result,
                        timeOutDate: this.datePipe.transform(result.timesOutAt, 'medium'),
                        timeoutString: t,
                    })),
                )),
            ).subscribe(
              (pairInfo) => this.pairInfo = pairInfo,
            ));
        } else {
            this.pairInfo = null;
        }
    }

    private deleteToken(tokenName: string): Subscription {
        return this.pairInfoService.delete(tokenName).subscribe(
            (deleteRes) => {
                if (!deleteRes.ok) {
                    console.log('Failed to delete stale MetaToken: '
                      + JSON.stringify(deleteRes.body));
                }
            },
        );
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
export class SubjectPairPopupComponent extends ObservablePopupComponent {

    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(SubjectPairDialogComponent, params['login']);
    }
}
