import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SensorDataService,
    SensorDataPopupService,
    SensorDataComponent,
    SensorDataDetailComponent,
    SensorDataDialogComponent,
    SensorDataPopupComponent,
    SensorDataDeletePopupComponent,
    SensorDataDeleteDialogComponent,
    sensorDataRoute,
    sensorDataPopupRoute,
} from './';

const ENTITY_STATES = [
    ...sensorDataRoute,
    ...sensorDataPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        SensorDataComponent,
        SensorDataDetailComponent,
        SensorDataDialogComponent,
        SensorDataDeleteDialogComponent,
        SensorDataPopupComponent,
        SensorDataDeletePopupComponent,
    ],
    entryComponents: [
        SensorDataComponent,
        SensorDataDialogComponent,
        SensorDataPopupComponent,
        SensorDataDeleteDialogComponent,
        SensorDataDeletePopupComponent,
    ],
    providers: [
        SensorDataService,
        SensorDataPopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalSensorDataModule {}
