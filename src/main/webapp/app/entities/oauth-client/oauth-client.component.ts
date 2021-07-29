import { Component, OnDestroy, OnInit } from '@angular/core';
import { AlertService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';

import { EventManager } from '../../shared/util/event-manager.service';
import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-oauth-client',
    templateUrl: './oauth-client.component.html',
})
export class OAuthClientComponent implements OnInit, OnDestroy {
    oauthClients: OAuthClient[];
    eventSubscriber: Subscription;
    objectKeys = Object.keys;

    times = {
        day: 86400,
        hour: 3600,
        minute: 60,
        second: 1,
    };

    constructor(
            private oauthClientService: OAuthClientService,
            private alertService: AlertService,
            private eventManager: EventManager,
    ) {
    }

    loadAll() {
        this.oauthClientService.query().subscribe(
                (res: HttpResponse<any>) => {
                    this.oauthClients = res.body;
                },
                (res: HttpErrorResponse) => this.onError(res.message),
        );
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInOAuthClients();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: OAuthClient) {
        return item.clientId;
    }

    registerChangeInOAuthClients() {
        this.eventSubscriber = this.eventManager.subscribe('oauthClientListModification', () => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    public convertSeconds(seconds: number) {
        let time_string = '';
        let plural = '';
        for (const key in this.times) {
            if (Math.floor(seconds / this.times[key]) > 0) {
                if (Math.floor(seconds / this.times[key]) > 1) {
                    plural = 's';
                } else {
                    plural = '';
                }

                time_string += Math.floor(seconds / this.times[key]).toString() + ' ' + key.toString() + plural + ' ';
                seconds = seconds - this.times[key] * Math.floor(seconds / this.times[key]);

            }
        }
        return time_string;
    }
}
