import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import { ManagementPortalSharedSourceModule } from '../../shared/source/source.module';
import {
    OrganizationComponent,
    OrganizationDeleteDialogComponent,
    OrganizationDeletePopupComponent,
    OrganizationDetailComponent,
    OrganizationDialogComponent,
    OrganizationPopupComponent,
    organizationPopupRoute,
    organizationRoute,
} from './';
import {ManagementPortalSharedGroupModule} from "../../shared/group/group.module";
import {ManagementPortalProjectModule} from "../project/project.module";
import {ManagementPortalSharedSubjectModule} from "../../shared/subject/subject.module";
import {ManagementPortalSharedPermissionModule} from "../../shared/permission/permissions.module";

const ENTITY_STATES = [
    ...organizationRoute,
    ...organizationPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalProjectModule,
        ManagementPortalSharedSourceModule,
        ManagementPortalSharedSubjectModule,
        ManagementPortalSharedGroupModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        ManagementPortalSharedPermissionModule,
    ],
    declarations: [
        OrganizationComponent,
        OrganizationDetailComponent,
        OrganizationDialogComponent,
        OrganizationDeleteDialogComponent,
        OrganizationPopupComponent,
        OrganizationDeletePopupComponent,
    ],
    entryComponents: [
        OrganizationComponent,
        OrganizationDialogComponent,
        OrganizationPopupComponent,
        OrganizationDeleteDialogComponent,
        OrganizationDeletePopupComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalOrganizationModule {
}
