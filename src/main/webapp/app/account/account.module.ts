import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../shared';

import {
    accountState,
    ActivateComponent,
    PasswordComponent,
    PasswordResetFinishComponent,
    PasswordResetInitComponent,
    PasswordStrengthBarComponent,
    SettingsComponent,
} from './';

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(accountState, {useHash: true}),
    ],
    declarations: [
        ActivateComponent,
        PasswordComponent,
        PasswordStrengthBarComponent,
        PasswordResetInitComponent,
        PasswordResetFinishComponent,
        SettingsComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalAccountModule {
}
