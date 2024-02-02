import { Component, OnDestroy, OnInit } from '@angular/core';

import { Principal } from '../../shared';
import { Subscription } from 'rxjs';
import {PasswordResetInit} from "../password-reset/init/password-reset-init.service";

@Component({
    selector: 'jhi-password',
    templateUrl: './password.component.html',
})
export class PasswordComponent implements OnDestroy {
    doNotMatch: string;
    weakPassword: string;
    error: string;
    success: string;
    password: string;
    confirmPassword: string;

    private subscriptions = new Subscription();

    constructor(
        private passwordResetInit: PasswordResetInit,
        public principal: Principal,
    ) {
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }


    requestPasswordChange() {
        this.principal.account$.subscribe(res => {
            this.subscriptions.add(this.passwordResetInit.save(res.email).subscribe(() => {
                this.success = 'OK';
            }, (response) => {
                this.success = null;
                this.error = 'ERROR';
            }));
        })
    }
}
