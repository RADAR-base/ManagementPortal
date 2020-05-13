import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, EventManager, JhiLanguageService } from 'ng-jhipster';
import { OAuthClientPopupService } from './oauth-client-popup.service';

import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-oauth-client-dialog',
    templateUrl: './oauth-client-dialog.component.html',
})
export class OAuthClientDialogComponent implements OnInit {
    readonly authorities: string[];
    readonly availableGrants: string[];
    client: OAuthClient;
    isSaving: boolean;
    grantTypes: any[];
    scopeList: string;
    redirectUriList: string;
    resourcesList: string;
    autoApproveScopeList: string;
    dynamicRegistration: boolean;
    emptySecret: boolean;
    dynamicRegistrationKey: string;
    newClient: boolean;
    protectedClient: boolean;

    constructor(
            public activeModal: NgbActiveModal,
            private jhiLanguageService: JhiLanguageService,
            private alertService: AlertService,
            private oauthClientSerivce: OAuthClientService,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('oauthClient');
        this.availableGrants = ['authorization_code', 'implicit', 'client_credentials', 'refresh_token', 'password'];
        this.authorities = ['ROLE_SYS_ADMIN'];
        this.isSaving = false;
    }

    ngOnInit() {
        // transform array of authorized grant types to array of objects with name and authorized property, so we can bind a checkbox to the authorized property
        this.grantTypes = this.availableGrants.map(grant => ({
            name: grant,
            authorized: this.client.authorizedGrantTypes.indexOf(grant) > -1,
        }));
        // transform array of scopes to a comma seperated string so we can bind a single text box to it
        this.scopeList = this.client.scope.join(', ');
        // transform array of resources to a comma seperated string so we can bind a single text box to it
        this.resourcesList = this.client.resourceIds.join(', ');
        this.redirectUriList = this.client.registeredRedirectUri ? this.client.registeredRedirectUri.join(', ') : '';
        // transform array of auto-approve scopes to a comma seperated string so we can bind a single text box to it
        this.autoApproveScopeList = this.client.autoApproveScopes ? this.client.autoApproveScopes.join(', ') : '';
        this.dynamicRegistrationKey = 'dynamic_registration';
        this.dynamicRegistration = this.client.additionalInformation[this.dynamicRegistrationKey] === 'true';
        // are we creating a new client?
        this.newClient = this.client.clientId === '';
        this.protectedClient = this.client.additionalInformation['protected'] === 'true';
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        // transform the authorized grant objects back to an array of authorized grants
        this.client.authorizedGrantTypes = [];
        this.grantTypes.forEach((grant) => {
            if (grant.authorized) {
                this.client.authorizedGrantTypes.push(grant.name);
            }
        });
        // transform the scope string back to an array
        this.client.scope = this.listStringToArray(this.scopeList);
        // transform the resources string back to an array
        this.client.resourceIds = this.listStringToArray(this.resourcesList);
        // transform the auto approve scopes back to an array
        this.client.autoApproveScopes = this.listStringToArray(this.autoApproveScopeList);

        this.client.registeredRedirectUri = this.listStringToArray(this.redirectUriList);
        if (this.dynamicRegistration) {
            this.client.additionalInformation[this.dynamicRegistrationKey] = true;
        } else {
            delete this.client.additionalInformation[this.dynamicRegistrationKey];
        }

        if (this.emptySecret) {
            this.client.clientSecret = '';
        }

        if (!this.newClient) {
            this.oauthClientSerivce.update(this.client)
            .subscribe((res: OAuthClient) =>
                    this.onSaveSuccess(res), (res: HttpErrorResponse) => this.onError(res.message));
        } else {
            this.oauthClientSerivce.create(this.client)
            .subscribe((res: OAuthClient) =>
                    this.onSaveSuccess(res), (res: HttpErrorResponse) => this.onError(res.message));
        }
    }

    // convert comma seperated list of values to array
    private listStringToArray(list: string) {
        const result = [];
        list.split(',').forEach((value) => {
            const trimmed = value.trim();
            if (trimmed !== '') {
                result.push(trimmed);
            }
        });
        return result;
    }

    private onSaveSuccess(result: OAuthClient) {
        this.eventManager.broadcast({name: 'oauthClientListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    generateRandomSecret() {
        const text = [];
        const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

        for (let i = 0; i < 36; i++) {
            text.push(possible.charAt(Math.floor(Math.random() * possible.length)));
        }

        this.client.clientSecret = text.join('');
    }
}

@Component({
    selector: 'jhi-oauth-client-popup',
    template: '',
})
export class OAuthClientPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private oauthClientPopupService: OAuthClientPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.oauthClientPopupService
                    .open(OAuthClientDialogComponent, params['clientId']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
