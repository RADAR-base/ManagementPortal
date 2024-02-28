import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { AuthServerProvider } from '../auth/auth-oauth2.service';
import { Principal } from '../auth/principal.service';
import { first, tap } from 'rxjs/operators';
import { Account } from "../user/account.model";
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoginService {
    constructor(
            private principal: Principal,
            private authServerProvider: AuthServerProvider,
            private translateService: TranslateService
    ) {
    }

    login(credentials): Observable<Account> {
        return this.authServerProvider.login(credentials).pipe(
          tap(
            (account) => {
              this.principal.authenticate(account);
              // After the login the language will be changed to
              // the language selected by the user during his registration
              if (account && account.langKey) {
                this.translateService.use(account.langKey);
              }
            },
            () => this.logout()
          ),
        );
    }

    requestOtpCode(credentials):  Observable<any> {
        return this.authServerProvider.requestOtpCode(credentials);
    }

    submitOptCode(credentials): Observable<any> {
        return this.authServerProvider.submitOPTCode(credentials);
    }

    logout() {
        this.authServerProvider.logout();
        this.principal.authenticate(null);
    }
}
