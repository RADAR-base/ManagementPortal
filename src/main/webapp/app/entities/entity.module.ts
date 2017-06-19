import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ManagementPortalSourceModule } from './source/source.module';
import { ManagementPortalDeviceTypeModule } from './device-type/device-type.module';
import { ManagementPortalProjectModule } from './project/project.module';
// import { ManagementPortalStudyModule } from './study/study.module';
import { ManagementPortalSensorDataModule } from './sensor-data/sensor-data.module';
import { ManagementPortalPatientModule } from './patient/patient.module';
import {ManagementPortalRoleModule} from "./role/role.module";
// import { ManagementPortalUsrModule } from './usr/usr.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalSourceModule,
        ManagementPortalDeviceTypeModule,
        ManagementPortalProjectModule,
        // ManagementPortalStudyModule,
        ManagementPortalSensorDataModule,
        ManagementPortalRoleModule,
        ManagementPortalPatientModule,
        // ManagementPortalUsrModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalEntityModule {}
