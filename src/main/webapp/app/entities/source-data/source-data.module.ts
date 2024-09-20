import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../../shared';
import {
    SourceDataComponent,
    SourceDataDeleteDialogComponent,
    SourceDataDeletePopupComponent,
    SourceDataDetailComponent,
    SourceDataDialogComponent,
    SourceDataPopupComponent,
    sourceDataPopupRoute,
    sourceDataRoute,
} from './';

const ENTITY_STATES = [
    ...sourceDataRoute,
    ...sourceDataPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
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
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalSourceDataModule {
}
