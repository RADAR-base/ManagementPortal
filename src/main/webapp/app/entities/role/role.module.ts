import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    RoleService,
    RolePopupService,
    RoleDetailComponent,
    RoleDialogComponent,
    RolePopupComponent,
    roleRoute,
    rolePopupRoute,
} from './';

const ENTITY_STATES = [
    ...roleRoute,
    ...rolePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        RoleDetailComponent,
        RoleDialogComponent,
        RolePopupComponent,
    ],
    entryComponents: [
        RolePopupComponent,
        RoleDialogComponent,
    ],
    providers: [
        RoleService,
        RolePopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalRoleModule {}
