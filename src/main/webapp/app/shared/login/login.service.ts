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

    login(credentials, callback?) {
        const cb = callback || function() {};

        return new Promise((resolve, reject) => {
            this.authServerProvider.login(credentials).subscribe((data) => {
                this.principal.identity(true).then((account) => {
                    // After the login the language will be changed to
                    // the language selected by the user during his registration
                    if (account !== null) {
                        this.translateService.use(account.langKey);
                    }
                    resolve(data);
                });
                return cb();
            }, (err) => {
                this.logout();
                reject(err);
                return cb(err);
            });
        });
    }

    logout() {
        this.authServerProvider.logout().subscribe();
        this.principal.authenticate(null);
    }
}
