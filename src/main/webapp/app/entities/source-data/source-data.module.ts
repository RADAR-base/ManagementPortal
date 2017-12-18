import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SourceDataService,
    SourceDataPopupService,
    SourceDataComponent,
    SourceDataDetailComponent,
    SourceDataDialogComponent,
    SourceDataPopupComponent,
    SourceDataDeletePopupComponent,
    SourceDataDeleteDialogComponent,
    sourceDataRoute,
    sourceDataPopupRoute,
} from './';
import {SourceDataResolvePagingParams} from "./source-data.route";

const ENTITY_STATES = [
    ...sourceDataRoute,
    ...sourceDataPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        SourceDataComponent,
        SourceDataDetailComponent,
        SourceDataDialogComponent,
        SourceDataDeleteDialogComponent,
        SourceDataPopupComponent,
        SourceDataDeletePopupComponent,
    ],
    entryComponents: [
        SourceDataComponent,
        SourceDataDialogComponent,
        SourceDataPopupComponent,
        SourceDataDeleteDialogComponent,
        SourceDataDeletePopupComponent,
    ],
    providers: [
        SourceDataService,
        SourceDataPopupService,
        SourceDataResolvePagingParams
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalSourceDataModule {}
