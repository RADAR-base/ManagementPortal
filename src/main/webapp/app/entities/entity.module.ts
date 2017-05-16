import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ManagementPortalDeviceModule } from './device/device.module';
import { ManagementPortalDeviceTypeModule } from './device-type/device-type.module';
import { ManagementPortalProjectModule } from './project/project.module';
// import { ManagementPortalStudyModule } from './study/study.module';
import { ManagementPortalSensorDataModule } from './sensor-data/sensor-data.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalDeviceModule,
        ManagementPortalDeviceTypeModule,
        ManagementPortalProjectModule,
        // ManagementPortalStudyModule,
        ManagementPortalSensorDataModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalEntityModule {}
