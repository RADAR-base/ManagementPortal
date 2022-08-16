import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {GroupComponent} from "./group.component";
import {GroupDialogComponent, GroupPopupComponent} from "./group-dialog.component";
import {GroupDeleteDialogComponent, GroupDeletePopupComponent} from "./group-delete-dialog.component";
import {groupPopupRoute, groupRoute} from "./group.route";

const ENTITY_STATES = [
    ...groupRoute,
    ...groupPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
    ],
    declarations: [
        GroupComponent,
        GroupDialogComponent,
        GroupDeleteDialogComponent,
        GroupPopupComponent,
        GroupDeletePopupComponent,
    ],
    entryComponents: [
        GroupComponent,
        GroupDialogComponent,
        GroupDeleteDialogComponent,
        GroupPopupComponent,
        GroupDeletePopupComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        GroupComponent,
    ],
})
export class ManagementPortalSharedGroupModule {
}
