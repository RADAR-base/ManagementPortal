import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ManagementPortalSourceModule } from './source/source.module';
import { ManagementPortalDeviceTypeModule } from './device-type/device-type.module';
import { ManagementPortalProjectModule } from './project/project.module';
import { ManagementPortalSensorDataModule } from './sensor-data/sensor-data.module';
import {ManagementPortalRoleModule} from "./role/role.module";
import {ManagementPortalSubjectModule} from "./subject/general.subject.module";
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalSourceModule,
        ManagementPortalDeviceTypeModule,
        ManagementPortalProjectModule,
        ManagementPortalSensorDataModule,
        ManagementPortalRoleModule,
        ManagementPortalSubjectModule
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalEntityModule {}
