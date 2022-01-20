import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { LoginModalService } from '../../../shared';

import { PasswordResetFinish } from './password-reset-finish.service';
import { Password } from '../../password/password.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'jhi-password-reset-finish',
    templateUrl: './password-reset-finish.component.html',
})
export class PasswordResetFinishComponent implements OnInit, AfterViewInit {
    @ViewChild('passwordField') passwordField: ElementRef | null;

    confirmPassword: string;
    weakPassword: string;
    doNotMatch: string;
    error: string;
    keyMissing: boolean;
    resetAccount: any;
    success: string;
    modalRef: NgbModalRef;
    key: string;
    private subscriptions = new Subscription();

    constructor(
            private passwordResetFinish: PasswordResetFinish,
            private passwordService: Password,
            private loginModalService: LoginModalService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscriptions.add(this.route.queryParams.subscribe((params) => {
            this.key = params['key'];
        }));
        this.resetAccount = {};
        this.keyMissing = !this.key;
    }

    ngAfterViewInit() {
        if (this.passwordField) {
            this.passwordField.nativeElement.focus();
        }
        this.subscriptions.unsubscribe();
        this.modalRef?.dismiss();
    }

    finishReset() {
        this.error = null;
        this.success = null;
        this.doNotMatch = null;
        this.weakPassword = null;
        if (this.passwordService.measureStrength(this.resetAccount.password) < 40) {
            this.weakPassword = 'ERROR';
        }
        if (this.resetAccount.password !== this.confirmPassword) {
            this.doNotMatch = 'ERROR';
        }

        if (this.weakPassword == null && this.doNotMatch == null) {
            this.subscriptions.add(this.passwordResetFinish.save({
                key: this.key,
                newPassword: this.resetAccount.password,
            }).subscribe(() => {
                this.success = 'OK';
            }, () => {
                this.error = 'ERROR';
            }));
        }
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
