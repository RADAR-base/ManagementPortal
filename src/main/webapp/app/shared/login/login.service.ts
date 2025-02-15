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

    login(accessToken: string): Observable<Account> {
      return this.authServerProvider.login(accessToken).pipe(
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

    logout() {
        this.authServerProvider.logout().subscribe();
        this.principal.authenticate(null);
    }
}
