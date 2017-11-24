import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule} from '@angular/forms';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
OAuthClientService,
OAuthClientComponent,
OAuthClientPopupService,
OAuthClientDialogComponent,
OAuthClientPopupComponent,
OAuthClientDeleteDialogComponent,
OAuthClientDeletePopupComponent,
oauthClientRoute,
oauthClientPopupRoute
} from './';


const ENTITY_STATES = [
    ...oauthClientRoute,
    ...oauthClientPopupRoute
];


@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true }),
        FormsModule
    ],
    declarations: [
        OAuthClientComponent,
        OAuthClientDialogComponent,
        OAuthClientPopupComponent,
        OAuthClientDeleteDialogComponent,
        OAuthClientDeletePopupComponent
    ],
    entryComponents: [
        OAuthClientComponent,
        OAuthClientDialogComponent,
        OAuthClientPopupComponent,
        OAuthClientDeleteDialogComponent,
        OAuthClientDeletePopupComponent
    ],
    providers: [
        OAuthClientService,
        OAuthClientPopupService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalOAuthClientModule {}
