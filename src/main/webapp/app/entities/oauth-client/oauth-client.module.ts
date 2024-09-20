import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../../shared';
import {
    OAuthClientComponent,
    OAuthClientDeleteDialogComponent,
    OAuthClientDeletePopupComponent,
    OAuthClientDialogComponent,
    OAuthClientPopupComponent,
    oauthClientPopupRoute,
    oauthClientRoute,
} from './';

const ENTITY_STATES = [
    ...oauthClientRoute,
    ...oauthClientPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        FormsModule,
    ],
    declarations: [
        OAuthClientComponent,
        OAuthClientDialogComponent,
        OAuthClientPopupComponent,
        OAuthClientDeleteDialogComponent,
        OAuthClientDeletePopupComponent,
    ],
    entryComponents: [
        OAuthClientComponent,
        OAuthClientDialogComponent,
        OAuthClientPopupComponent,
        OAuthClientDeleteDialogComponent,
        OAuthClientDeletePopupComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalOAuthClientModule {
}
