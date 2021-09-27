import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    RoleDetailComponent,
    roleRoute,
} from './';

const ENTITY_STATES = [
    ...roleRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
    ],
    declarations: [
        RoleDetailComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalRoleModule {
}
