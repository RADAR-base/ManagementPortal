import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    DeviceTypeService,
    DeviceTypePopupService,
    DeviceTypeComponent,
    DeviceTypeDetailComponent,
    DeviceTypeDialogComponent,
    DeviceTypePopupComponent,
    DeviceTypeDeletePopupComponent,
    DeviceTypeDeleteDialogComponent,
    deviceTypeRoute,
    deviceTypePopupRoute,
} from './';

const ENTITY_STATES = [
    ...deviceTypeRoute,
    ...deviceTypePopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        DeviceTypeComponent,
        DeviceTypeDetailComponent,
        DeviceTypeDialogComponent,
        DeviceTypeDeleteDialogComponent,
        DeviceTypePopupComponent,
        DeviceTypeDeletePopupComponent,
    ],
    entryComponents: [
        DeviceTypeComponent,
        DeviceTypeDialogComponent,
        DeviceTypePopupComponent,
        DeviceTypeDeleteDialogComponent,
        DeviceTypeDeletePopupComponent,
    ],
    providers: [
        DeviceTypeService,
        DeviceTypePopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalDeviceTypeModule {}
