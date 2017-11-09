import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {GeneralSourceComponent} from "./general-source.component";
import {sourcePopupRoute, sourceRoute} from "./general-source.route";
import {
    GeneralSourceDialogComponent,
    GeneralSourcePopupComponent
} from "./general-source-dialog.component";
import {GeneralSourcePopupService} from "./general-source-popup.service";
import {ManagementPortalSharedSourceModule} from "../../shared/source/source.module";

const ENTITY_STATES = [
    ...sourceRoute,
    ...sourcePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSourceModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        GeneralSourceComponent,
        GeneralSourceDialogComponent,
        GeneralSourcePopupComponent

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
    exports: [

    ],
})
export class ManagementPortalSourceModule {}
