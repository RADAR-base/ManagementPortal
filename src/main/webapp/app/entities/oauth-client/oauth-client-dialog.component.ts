import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';
import { OAuthClientPopupService } from './oauth-client-popup.service';

@Component({
    selector: 'jhi-oauth-client-dialog',
    templateUrl: './oauth-client-dialog.component.html'
})
export class OAuthClientDialogComponent implements OnInit  {

    client: OAuthClient;
    isSaving: boolean;
    showSecret: boolean;
    authorities: any[];
    grantTypes: any[];
    scopeList: string;
    resourcesList: string;
    autoApproveScopeList: string;
    availableGrants: string[];
    dynamicRegistration: boolean;
    dynamicRegistrationKey: string;
    newClient: boolean;

    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private oauthClientSerivce: OAuthClientService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['oauthclient']);
        this.availableGrants = ['authorization_code', 'implicit', 'client_credentials', 'refresh_token', 'password'];
    }

    ngOnInit() {
        this.isSaving = false;
        // transform array of authorized grant types to array of objects with name and authorized property, so we can bind a checkbox to the authorized property
        this.grantTypes = [];
        this.availableGrants.forEach((grant) => {
            this.grantTypes.push({"name": grant, "authorized": this.client.authorizedGrantTypes.indexOf(grant) > -1});
        });
        // transform array of scopes to a comma seperated string so we can bind a single text box to it
        this.scopeList = this.client.scope.join(", ");
        // transform array of resources to a comma seperated string so we can bind a single text box to it
        this.resourcesList = this.client.resourceIds.join(", ");
        // transform array of auto-approve scopes to a comma seperated string so we can bind a single text box to it
        this.autoApproveScopeList = this.client.autoApproveScopes.join(", ");
        this.dynamicRegistrationKey = 'dynamic_registration';
        this.dynamicRegistration = this.client.additionalInformation[this.dynamicRegistrationKey] 
                ? this.client.additionalInformation[this.dynamicRegistrationKey] : false;
        this.authorities = ['ROLE_SYS_ADMIN'];
        // are we creating a new client?
        this.newClient = this.client.clientId == '';
        // if it's a new client, don't hide secret
        this.showSecret = this.newClient;
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
        if (this.dynamicRegistration) {
            this.client.additionalInformation[this.dynamicRegistrationKey] = true;
        }
        else {
            delete this.client.additionalInformation[this.dynamicRegistrationKey];
        }
        if (!this.newClient) {
            this.oauthClientSerivce.update(this.client)
                .subscribe((res: OAuthClient) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.oauthClientSerivce.create(this.client)
                .subscribe((res: OAuthClient) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    // convert comma seperated list of values to array
    private listStringToArray(list: string) {
        var result = [];
        list.split(',').forEach((value) => {
            var trimmed = value.trim();
            if (trimmed != '') {
                result.push(trimmed);
            }
        });
        return result;
    }

    private onSaveSuccess(result: OAuthClient) {
        this.eventManager.broadcast({ name: 'oauthClientListModification', content: 'OK'});
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

    trackByClientId(index: number, item: OAuthClient) {
        return item.clientId;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }

    toggleShowSecret() {
        this.showSecret = !this.showSecret;
    }

    generateRandomSecret() {
        var text = [];
        const possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      
        for (var i = 0; i < 36; i++) {
          text.push(possible.charAt(Math.floor(Math.random() * possible.length)));
        }

        this.client.clientSecret = text.join('');
    }
}

@Component({
    selector: 'jhi-oauth-client-popup',
    template: ''
})
export class OAuthClientPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private oauthClientPopupService: OAuthClientPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['clientId'] ) {
                this.modalRef = this.oauthClientPopupService
                    .open(OAuthClientDialogComponent, params['clientId']);
            } else {
                this.modalRef = this.oauthClientPopupService
                    .open(OAuthClientDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
