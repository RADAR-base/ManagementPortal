import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { Router, UrlSegment } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { StateStorageService } from '..';
import { EventManager } from '../util/event-manager.service';

import { LoginService } from './login.service';

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

    constructor(
            private eventManager: EventManager,
            private loginService: LoginService,
            private stateStorageService: StateStorageService,
            private router: Router,
            public activeModal: NgbActiveModal,
    ) {
        this.credentials = {};
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
        this.authenticationError = false;
        this.activeModal.dismiss('cancel');
    }

    login() {
        this.loginService.login({
            username: this.username,
            password: this.password,
            rememberMe: this.rememberMe,
        }).then(() => {
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

            // previousState was set in the authExpiredInterceptor before being redirected to login modal.
            // since login is succesful, go to stored previousState and clear previousState
            const previousState = this.stateStorageService.getPreviousState();
            if (previousState) {
                this.stateStorageService.resetPreviousState();
                return this.router.navigate(
                    previousState.path.map(p => p.path ? p.path : p.parameters),
                    {queryParams: previousState.queryParams},
                );
            }

            const redirect = this.stateStorageService.getUrl();
            if (redirect) {
                this.stateStorageService.storeUrl(null);
                return this.router.navigate([redirect]);
            } else {
                return this.router.navigate(['/']);
            }
        }).catch(() => {
            this.authenticationError = true;
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
}
