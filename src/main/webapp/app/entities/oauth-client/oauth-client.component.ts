import { Component, OnInit, OnDestroy } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';
import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';

@Component({
    selector: 'jhi-oauth-client',
    templateUrl: './oauth-client.component.html'
})
export class OAuthClientComponent implements OnInit, OnDestroy {
    oauthClients: OAuthClient[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private oauthClientService: OAuthClientService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['oauthClient']);
    }

    loadAll() {
        this.oauthClientService.query().subscribe(
            (res: Response) => {
                this.oauthClients = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    ngOnInit() {
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInOAuthClients();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: OAuthClient) {
        return item.clientId;
    }

    registerChangeInOAuthClients() {
        this.eventSubscriber = this.eventManager.subscribe('oauthClientListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
