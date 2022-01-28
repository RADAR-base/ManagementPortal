import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SourceTypeComponent,
    SourceTypeDeleteDialogComponent,
    SourceTypeDeletePopupComponent,
    SourceTypeDetailComponent,
    SourceTypeDialogComponent,
    SourceTypePopupComponent,
    sourceTypePopupRoute,
    sourceTypeRoute,
} from './';

const ENTITY_STATES = [
    ...sourceTypeRoute,
    ...sourceTypePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        FormsModule,
    ],
    declarations: [
        SourceTypeComponent,
        SourceTypeDetailComponent,
        SourceTypeDialogComponent,
        SourceTypeDeleteDialogComponent,
        SourceTypePopupComponent,
        SourceTypeDeletePopupComponent,
    ],
    entryComponents: [
        SourceTypeComponent,
        SourceTypeDialogComponent,
        SourceTypePopupComponent,
        SourceTypeDeleteDialogComponent,
        SourceTypeDeletePopupComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalSourceTypeModule {
}
