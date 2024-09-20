import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../../shared';
import {RevisionComponent} from './revision.component';
import {revisionRoute} from './revision.route';

const ENTITY_STATES = [
    ...revisionRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
    ],
    declarations: [
        RevisionComponent,
    ],
    entryComponents: [
        RevisionComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        RevisionComponent,
    ],
})
export class ManagementPortalRevisionModule {
}
