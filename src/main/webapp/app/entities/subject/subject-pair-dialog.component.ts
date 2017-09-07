import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Response} from '@angular/http';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';

import {Subject} from './subject.model';
import {SubjectPopupService} from './subject-popup.service';
import {SubjectService} from './subject.service';
import {OAuthClient} from '../oauth-client/oauth-client.model';
import {OAuthClientService} from '../oauth-client/oauth-client.service';
import {OAuthClientPairInfoService} from '../oauth-client/oauth-client-pair-info.service';
import {MinimalSource} from '../source/source.model';

@Component({
    selector: 'jhi-subject-pair-dialog',
    templateUrl: './subject-pair-dialog.component.html',
    providers: [OAuthClientService, OAuthClientPairInfoService]
})
export class SubjectPairDialogComponent implements OnInit {

    subject: Subject;
    authorities: any[];
    oauthClients: OAuthClient[];
    oauthClientPairInfo: string;
    selectedClient: OAuthClient;
    showQRCode = false;

    sources: MinimalSource[];

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private oauthClientService: OAuthClientService,
                private oauthClientPairInfoService: OAuthClientPairInfoService,
                private eventManager: EventManager) {
        this.jhiLanguageService.setLocations(['subject']);
    }

    ngOnInit() {
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.oauthClientService.query().subscribe(
            (res) => {
                // only keep clients that allow authorization_code and refresh_token
                this.oauthClients = res.json()
                    .filter(c => c.authorizedGrantTypes.includes('authorization_code') &&
                                 c.authorizedGrantTypes.includes('refresh_token'));
            });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    trackOAuthClientById(index: number, item: OAuthClient) {
        return item.clientId;
    }

    updateQRCode() {
        if (this.selectedClient != null) {
            this.oauthClientPairInfoService.get(this.selectedClient, this.subject).subscribe(
                (res) => {
                    this.oauthClientPairInfo = res.text();
                    this.showQRCode = true;
                });
        }
        else {
            this.showQRCode = false;
            this.oauthClientPairInfo = "";
        }
    }


}

@Component({
    selector: 'jhi-subject-pair-popup',
    template: ''
})
export class SubjectPairPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if (params['id']) {
                this.modalRef = this.subjectPopupService
                .open(SubjectPairDialogComponent, params['id']);
            } else {
                this.modalRef = this.subjectPopupService
                .open(SubjectPairDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
