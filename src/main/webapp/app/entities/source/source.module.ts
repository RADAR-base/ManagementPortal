import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SourceService,
    SourcePopupService,
    SourceComponent,
    SourceDetailComponent,
    SourceDialogComponent,
    SourcePopupComponent,
    SourceDeletePopupComponent,
    SourceDeleteDialogComponent,
    sourceRoute,
    sourcePopupRoute,
} from './';

const ENTITY_STATES = [
    ...sourceRoute,
    ...sourcePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        SourceComponent,
        SourceDetailComponent,
        SourceDialogComponent,
        SourceDeleteDialogComponent,
        SourcePopupComponent,
        SourceDeletePopupComponent,
    ],
    entryComponents: [
        SourceComponent,
        SourceDialogComponent,
        SourcePopupComponent,
        SourceDeleteDialogComponent,
        SourceDeletePopupComponent,
    ],
    providers: [
        SourceService,
        SourcePopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalSourceModule {}
