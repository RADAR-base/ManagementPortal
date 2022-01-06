import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';

import { Account, AccountService, JhiLanguageHelper, Principal } from '../../shared';
import { filter, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'jhi-settings',
    templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit, OnDestroy {
    private subscription: Subscription = new Subscription();
    error: string;
    success: string;
    settingsAccount?: Account;
    previousLangKey: String;

    constructor(
            private account: AccountService,
            private principal: Principal,
            public languageHelper: JhiLanguageHelper,
            private translateService: TranslateService,
    ) {
    }

    ngOnInit() {
        this.subscription.add(this.registerChangesToAccount());
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
        const currentLangKey = this.settingsAccount.langKey;
        this.subscription.add(this.account.save(this.settingsAccount).pipe(
            tap(
                () => {
                    this.error = null;
                    this.success = 'OK';
                },
                () => {
                    this.success = null;
                    this.error = 'ERROR';
                }
            ),
            switchMap(() => this.principal.reset()),
        ).subscribe(
            () => {
                if (currentLangKey && currentLangKey !== this.previousLangKey) {
                    this.translateService.use(currentLangKey)
                    this.previousLangKey = currentLangKey;
                }
            }
        ));
    }
}
