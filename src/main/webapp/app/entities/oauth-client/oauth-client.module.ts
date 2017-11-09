import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
//import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
OAuthClientService,
OAuthClientComponent,
} from './';

/*
const ENTITY_STATES = [
...subjectRoute,
...subjectPopupRoute,
];
*/

@NgModule({
    imports: [
        ManagementPortalSharedModule
    ],
    declarations: [
        OAuthClientComponent
    ],
    entryComponents: [
        OAuthClientComponent
    ],
    providers: [
        OAuthClientService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalOAuthClientModule {}
