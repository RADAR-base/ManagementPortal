import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../../shared/util/event-manager.service';
import { OAuthClientPopupService } from './oauth-client-popup.service';

import { OAuthClient } from './oauth-client.model';
import { OAuthClientService } from './oauth-client.service';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-oauth-client-delete-dialog',
    templateUrl: './oauth-client-delete-dialog.component.html',
})
export class OAuthClientDeleteDialogComponent implements OnInit, OnDestroy {
    client: OAuthClient;
    protectedClient: boolean;

    private subscriptions: Subscription = new Subscription();

    constructor(
            private oauthClientService: OAuthClientService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    ngOnInit() {
        this.protectedClient = this.client.additionalInformation['protected'] === 'true';
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(clientId: string) {
        this.subscriptions.add(this.oauthClientService.delete(clientId).subscribe(() => {
            this.eventManager.broadcast({
                name: 'oauthClientListModification',
                content: 'Deleted OAuth Client',
            });
            this.activeModal.dismiss(true);
        }));
    }
}

@Component({
    selector: 'jhi-oauth-client-delete-popup',
    template: '',
})
export class OAuthClientDeletePopupComponent extends ObservablePopupComponent {
    constructor(
            route: ActivatedRoute,
            private oauthClientPopupService: OAuthClientPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.oauthClientPopupService.open(OAuthClientDeleteDialogComponent, params['clientId']);
    }
}
