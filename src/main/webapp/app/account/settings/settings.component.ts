import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';

import { Account, AccountService, JhiLanguageHelper, Principal } from '../../shared';
import { filter } from "rxjs/operators";

@Component({
    selector: 'jhi-settings',
    templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit, OnDestroy {
    private subscription: Subscription = new Subscription();
    error: string;
    success: string;
    settingsAccount?: Account;
    languages: any[];
    previousLangKey: String;

    constructor(
            private account: AccountService,
            private principal: Principal,
            private languageHelper: JhiLanguageHelper,
            private translateService: TranslateService,
    ) {
    }

    ngOnInit() {
        this.subscription.add(this.registerChangesToAccount());
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private registerChangesToAccount(): Subscription {
        return this.principal.account$.pipe(
            filter(a => !!a),
        ).subscribe(({activated, email, firstName, langKey, lastName, login}) => {
            this.settingsAccount = { activated, email, firstName, langKey, lastName, login };
            this.previousLangKey = this.settingsAccount.langKey;
        });
    }

    save() {
        this.account.save(this.settingsAccount).subscribe(async () => {
            this.error = null;
            this.success = 'OK';
            const currentLangKey = this.settingsAccount.langKey;
            await this.principal.reset().toPromise();
            if (currentLangKey && currentLangKey !== this.previousLangKey) {
                this.translateService.use(currentLangKey)
                this.previousLangKey = currentLangKey;
            }
        }, () => {
            this.success = null;
            this.error = 'ERROR';
        });
    }
}
