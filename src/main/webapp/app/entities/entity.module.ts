import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ManagementPortalSourceTypeModule } from './source-type/source-type.module';
import { ManagementPortalProjectModule } from './project/project.module';
import { ManagementPortalSourceDataModule } from './source-data/source-data.module';
import { ManagementPortalRoleModule } from "./role/role.module";
import { ManagementPortalSubjectModule } from "./subject/general.subject.module";
import { ManagementPortalSourceModule } from "./source/general-source.module";
import { ManagementPortalOAuthClientModule } from './oauth-client/oauth-client.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        ManagementPortalSourceModule,
        ManagementPortalSourceTypeModule,
        ManagementPortalProjectModule,
        ManagementPortalSourceDataModule,
        ManagementPortalRoleModule,
        ManagementPortalSubjectModule,
        ManagementPortalOAuthClientModule
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalEntityModule {}
