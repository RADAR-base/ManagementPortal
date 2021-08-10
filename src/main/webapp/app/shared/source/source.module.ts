import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SourceComponent,
    SourceDeleteDialogComponent,
    SourceDeletePopupComponent,
    SourceDetailComponent,
    SourceDialogComponent,
    SourcePopupComponent,
    sourcePopupRoute,
    sourceRoute,
} from './';

const ENTITY_STATES = [
    ...sourceRoute,
    ...sourcePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
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
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        SourceComponent,
    ],
})
export class ManagementPortalSharedSourceModule {
}
