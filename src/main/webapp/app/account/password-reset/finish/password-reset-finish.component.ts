import { AfterViewInit, Component, ElementRef, OnInit, Renderer } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { LoginModalService } from '../../../shared';

import { PasswordResetFinish } from './password-reset-finish.service';
import { Password } from '../../password/password.service';

@Component({
    selector: 'jhi-password-reset-finish',
    templateUrl: './password-reset-finish.component.html',
})
export class PasswordResetFinishComponent implements OnInit, AfterViewInit {
    confirmPassword: string;
    weakPassword: string;
    doNotMatch: string;
    error: string;
    keyMissing: boolean;
    resetAccount: any;
    success: string;
    modalRef: NgbModalRef;
    key: string;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private passwordResetFinish: PasswordResetFinish,
            private passwordService: Password,
            private loginModalService: LoginModalService,
            private route: ActivatedRoute,
            private elementRef: ElementRef, private renderer: Renderer,
    ) {
        this.jhiLanguageService.setLocations(['reset']);
    }

    ngOnInit() {
        this.route.queryParams.subscribe((params) => {
            this.key = params['key'];
        });
        this.resetAccount = {};
        this.keyMissing = !this.key;
    }

    ngAfterViewInit() {
        if (this.elementRef.nativeElement.querySelector('#password') !== null) {
            this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#password'), 'focus', []);
        }
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
            this.passwordResetFinish.save({
                key: this.key,
                newPassword: this.resetAccount.password,
            }).subscribe(() => {
                this.success = 'OK';
            }, () => {
                this.error = 'ERROR';
            });
        }
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
