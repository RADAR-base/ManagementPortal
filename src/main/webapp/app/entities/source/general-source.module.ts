import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../../shared';
import {ManagementPortalSharedSourceModule} from '../../shared/source/source.module';
import {GeneralSourceDialogComponent, GeneralSourcePopupComponent,} from './general-source-dialog.component';
import {GeneralSourcePopupService} from './general-source-popup.service';
import {GeneralSourceComponent} from './general-source.component';
import {sourcePopupRoute, sourceRoute} from './general-source.route';

const ENTITY_STATES = [
    ...sourceRoute,
    ...sourcePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSourceModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
    ],
    declarations: [
        GeneralSourceComponent,
        GeneralSourceDialogComponent,
        GeneralSourcePopupComponent,

    ],
    entryComponents: [
        GeneralSourceComponent,
        GeneralSourceDialogComponent,
        GeneralSourcePopupComponent,
    ],
    providers: [
        GeneralSourcePopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [],
})
export class ManagementPortalSourceModule {
}
