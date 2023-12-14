import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { Router, UrlSegment } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { AuthService, StateStorageService } from '..';
import { EventManager } from '../util/event-manager.service';

import { LoginService } from './login.service';
import { first } from 'rxjs/operators';

@Component({
    selector: 'jhi-login-modal',
    templateUrl: './login.component.html',
})
export class JhiLoginModalComponent implements AfterViewInit {
    @ViewChild('usernameField') usernameField: ElementRef;

    authenticationError: boolean;
    password: string;
    rememberMe: boolean;
    username: string;
    credentials: any;
    showOTPForm: boolean;
    optCode:string;
    errorMessage:string;

    constructor(
            private eventManager: EventManager,
            private loginService: LoginService,
            private router: Router,
            public activeModal: NgbActiveModal,
            private authService: AuthService,
    ) {
        this.credentials = {};
        this.errorMessage = null;
        this.showOTPForm = false;
    }

    ngAfterViewInit() {
        this.usernameField.nativeElement.focus();
    }

    cancel() {
        this.credentials = {
            username: null,
            password: null,
            rememberMe: true,
        };
        this.errorMessage = null;
        this.authenticationError = false;
        this.activeModal.dismiss('cancel');
    }

    login() {
        this.loginService.login({
            username: this.username,
            password: this.password,
            rememberMe: this.rememberMe,
        }).pipe(first()).toPromise().then(() => {
            this.authenticationError = false;
            this.activeModal.dismiss('login success');
            if (this.router.url === '/register' || (/activate/.test(this.router.url)) ||
                    this.router.url === '/finishReset' || this.router.url === '/requestReset') {
                return this.router.navigate(['']);
            }

            this.eventManager.broadcast({
                name: 'authenticationSuccess',
                content: 'Sending Authentication Success',
            });

            return this.authService.redirectBeforeUnauthenticated();
        }).catch(() => {
            this.authenticationError = true;
        }).then((isRedirected) => {
            if (!isRedirected) {
                return this.router.navigate(['/']);
            }
        });
    }

    register() {
        this.activeModal.dismiss('to state register');
        this.router.navigate(['/register']);
    }

    async requestResetPassword() {
        this.activeModal.dismiss('to state requestReset');
        await this.router.navigate(['/reset', 'request']);
    }

    shouldShowOTPForm(): boolean {
            return this.showOTPForm;
    }

    getOTPCode() {
         let credentials = {
            userName: this.username,
            password: this.password,
         };
        this.loginService.requestOtpCode(credentials).subscribe(
            () => {
            this.authenticationError = false;
            this.showOTPForm = true;
            },
            error => {
            this.authenticationError = true;
            this.errorMessage = "<strong>Failed to sign in!</strong> Please check your credentials and try again."
            }
        );
    }

    submitOPTCode() {
      let credentials = {
                userName: this.username,
                password: this.password,
                code:this.optCode
      };
      this.loginService.submitOptCode(credentials).subscribe(data => {
        this.login();
      },error => {
        this.authenticationError = true;
        this.errorMessage = "Wrong OTP code entered"

      });
    }
}
