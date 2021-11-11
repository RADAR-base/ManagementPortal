import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';

import { Account, AccountService, JhiLanguageHelper, Principal } from '../../shared';

@Component({
    selector: 'jhi-settings',
    templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit, OnDestroy {
    private subscription: Subscription;
    error: string;
    success: string;
    settingsAccount: Account;
    previousLangKey: String;

    constructor(
            private account: AccountService,
            private principal: Principal,
            public languageHelper: JhiLanguageHelper,
            private translateService: TranslateService,
    ) {
    }

    ngOnInit() {
        this.subscription = this.principal.account$.subscribe((account) => {
            this.settingsAccount = this.copyAccount(account);
            this.previousLangKey = this.settingsAccount.langKey;
        });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    save() {
        const currentLangKey = this.settingsAccount.langKey;
        this.account.save(this.settingsAccount).subscribe(async () => {
            this.error = null;
            this.success = 'OK';
            await this.principal.reset();
            if (currentLangKey && currentLangKey !== this.previousLangKey) {
                this.translateService.use(currentLangKey)
                this.previousLangKey = currentLangKey;
            }
        }, () => {
            this.success = null;
            this.error = 'ERROR';
        });
    }

    copyAccount(account): Account {
        return {
            activated: account.activated,
            email: account.email,
            firstName: account.firstName,
            langKey: account.langKey,
            lastName: account.lastName,
            login: account.login,
        };
    }
}
