import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { LoginModalService } from '../../shared';

import { Activate } from './activate.service';
import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
    selector: 'jhi-activate',
    templateUrl: './activate.component.html',
})
export class ActivateComponent implements OnInit, OnDestroy {
    error: string;
    success: string;
    modalRef: NgbModalRef;
    subscription: Subscription;
    private loginUrl = 'api/redirect/login';

    constructor(
            private activate: Activate,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.queryParams.pipe(
            switchMap((params) => this.activate.get(params['key'])),
        ).subscribe(
            () => {
                this.error = null;
                this.success = 'OK';
            },
            () => {
                this.success = null;
                this.error = 'ERROR';
            },
        );
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.modalRef?.dismiss();
    }

    login() {
        window.location.href = this.loginUrl
    }
}
