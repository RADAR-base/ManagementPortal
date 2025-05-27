import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import { RadarDataComponent } from './radar-data.component';
import { radarDataRoute } from './radar-data.route';
import { ManagementPortalSharedSubjectModule } from 'app/shared/subject/subject.module';
const ENTITY_STATES = [
    ...radarDataRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSubjectModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
    ],
    declarations: [
        RadarDataComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalRadarDataModule {
}
