import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ManagementPortalDeviceModule } from './device/device.module';
import { ManagementPortalDeviceTypeModule } from './device-type/device-type.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalDeviceModule,
        ManagementPortalDeviceTypeModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalEntityModule {}
