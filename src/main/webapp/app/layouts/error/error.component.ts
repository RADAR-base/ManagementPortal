import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { LoginModalService } from '../../shared';
import { Subscription } from "rxjs";
import {environment} from "../../../environments/environment";

@Component({
    selector: 'jhi-error',
    templateUrl: './error.component.html',
})
export class ErrorComponent implements OnInit, OnDestroy {
    errorMessage: string;
    error403: boolean;
    modalRef: NgbModalRef;
    private routeSubscription: Subscription;
    private loginUrl = 'api/redirect/login';

    constructor(
            private loginModalService: LoginModalService,
            private route: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.routeSubscription = this.route.url.subscribe((url) => {
            if (url[0].path === 'accessdenied') {
                this.error403 = true;
            }
        });
    }

    ngOnDestroy() {
        this.routeSubscription.unsubscribe();
    }

    login() {
        window.location.href =  this.loginUrl;
    }
}