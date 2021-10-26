import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { AuthServerProvider } from '../auth/auth-oauth2.service';
import { Principal } from '../auth/principal.service';

@Injectable({ providedIn: 'root' })
export class LoginService {

    constructor(
            private principal: Principal,
            private authServerProvider: AuthServerProvider,
            private translateService: TranslateService
    ) {
    }

    login(credentials): Promise<void> {
        return new Promise((resolve, reject) => {
            this.authServerProvider.login(credentials).subscribe((account) => {
                this.principal.authenticate(account);
                // After the login the language will be changed to
                // the language selected by the user during his registration
                if (account !== null) {
                    this.translateService.use(account.langKey);
                }
                resolve();
            }, (err) => {
                this.logout();
                reject(err);
            });
        });
    }

    logout() {
        this.authServerProvider.logout().subscribe();
        this.principal.authenticate(null);
    }
}
