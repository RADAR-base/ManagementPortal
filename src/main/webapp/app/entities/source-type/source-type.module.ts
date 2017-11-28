import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SourceTypeService,
    SourceTypePopupService,
    SourceTypeComponent,
    SourceTypeDetailComponent,
    SourceTypeDialogComponent,
    SourceTypePopupComponent,
    SourceTypeDeletePopupComponent,
    SourceTypeDeleteDialogComponent,
    sourceTypeRoute,
    sourceTypePopupRoute,
} from './';
import {FormsModule} from "@angular/forms";

const ENTITY_STATES = [
    ...sourceTypeRoute,
    ...sourceTypePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true }),
        FormsModule
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
    providers: [
        SourceTypeService,
        SourceTypePopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalSourceTypeModule {}
