import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';

import {PasswordResetInit} from './password-reset-init.service';
import {Subscription} from 'rxjs';

@Component({
    selector: 'jhi-password-reset-init',
    templateUrl: './password-reset-init.component.html',
})
export class PasswordResetInitComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('emailField') emailField: ElementRef;

    error: string;
    errorEmailNotExists: string;
    resetAccount: any;
    success: string;

    private subscriptions = new Subscription();

    constructor(
        private passwordResetInit: PasswordResetInit,
    ) {
    }

    ngOnInit() {
        this.resetAccount = {};
    }

    ngAfterViewInit() {
        this.emailField.nativeElement.focus();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    requestReset() {
        this.error = null;
        this.errorEmailNotExists = null;

        this.subscriptions.add(this.passwordResetInit.save(this.resetAccount.email).subscribe(() => {
            this.success = 'OK';
        }, (response) => {
            this.success = null;
            if (response.status === 400) {
                this.errorEmailNotExists = 'ERROR';
            } else {
                this.error = 'ERROR';
            }
        }));
    }
}
