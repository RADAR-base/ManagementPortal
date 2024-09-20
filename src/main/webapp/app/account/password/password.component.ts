import {Component, OnDestroy} from '@angular/core';

import {Principal} from '../../shared';
import {Password} from './password.service';
import {Subscription} from 'rxjs';

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
        private passwordService: Password,
        public principal: Principal,
    ) {
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    changePassword() {
        this.error = null;
        this.success = null;
        this.doNotMatch = null;
        this.weakPassword = null;
        if (this.passwordService.measureStrength(this.password) < 40) {
            this.weakPassword = 'ERROR';
        }
        if (this.password !== this.confirmPassword) {
            this.doNotMatch = 'ERROR';
        }

        if (this.weakPassword == null && this.doNotMatch == null) {
            this.subscriptions.add(this.passwordService.save(this.password).subscribe(
                () => this.success = 'OK',
                () => this.error = 'ERROR',
            ));
        }
    }
}
