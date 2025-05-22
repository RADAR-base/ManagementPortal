import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { ManagementPortalOAuthClientModule } from './oauth-client/oauth-client.module';
import { ManagementPortalProjectModule } from './project/project.module';
import { ManagementPortalRevisionModule } from './revision/revision.module';
import { ManagementPortalRoleModule } from './role/role.module';
import { ManagementPortalSourceDataModule } from './source-data/source-data.module';

import { ManagementPortalSourceTypeModule } from './source-type/source-type.module';
import { ManagementPortalSourceModule } from './source/general-source.module';
import { ManagementPortalSubjectModule } from './subject/general.subject.module';
import {ManagementPortalOrganizationModule} from "./organization/organization.module";
import { ManagementPortalRadarDataModule } from './radar-data';

/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalSourceModule,
        ManagementPortalSourceTypeModule,
        ManagementPortalOrganizationModule,
        ManagementPortalProjectModule,
        ManagementPortalSourceDataModule,
        ManagementPortalRoleModule,
        ManagementPortalSubjectModule,
        ManagementPortalOAuthClientModule,
        ManagementPortalRevisionModule,
        ManagementPortalRadarDataModule
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalEntityModule {
}
