import { Component, OnInit } from '@angular/core';

import { Principal } from '../../shared';
import { Password } from './password.service';

@Component({
    selector: 'jhi-password',
    templateUrl: './password.component.html',
})
export class PasswordComponent {
    doNotMatch: string;
    weakPassword: string;
    error: string;
    success: string;
    password: string;
    confirmPassword: string;

    constructor(
      private passwordService: Password,
      public principal: Principal,
    ) {
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
            this.passwordService.save(this.password)
                .subscribe(() => {
                    this.success = 'OK';
                }, () => {
                    this.error = 'ERROR';
                });
        }
    }
}
