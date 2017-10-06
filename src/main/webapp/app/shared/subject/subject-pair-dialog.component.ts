import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';

import {SubjectPopupService} from './subject-popup.service';
import {OAuthClientService} from "../../entities/oauth-client/oauth-client.service";
import {OAuthClientPairInfoService} from "../../entities/oauth-client/oauth-client-pair-info.service";
import {OAuthClient} from "../../entities/oauth-client/oauth-client.model";
import {Subject} from "./subject.model";

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

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private oauthClientService: OAuthClientService,
                private oauthClientPairInfoService: OAuthClientPairInfoService,
                private eventManager: EventManager) {
        this.jhiLanguageService.setLocations(['subject' , 'project' , 'projectStatus']);
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
            if (params['login']) {
                this.modalRef = this.subjectPopupService
                .open(SubjectPairDialogComponent, params['login']);
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
