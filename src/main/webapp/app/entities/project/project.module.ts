import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {ManagementPortalSharedModule} from '../../shared';
import {ManagementPortalSharedSourceModule} from '../../shared/source/source.module';
import {ManagementPortalSharedSubjectModule} from '../../shared/subject/subject.module';
import {
    ProjectComponent,
    ProjectDeleteDialogComponent,
    ProjectDeletePopupComponent,
    ProjectDetailComponent,
    ProjectDialogComponent,
    ProjectPopupComponent,
    projectPopupRoute,
    projectRoute,
} from './';
import {ManagementPortalSharedGroupModule} from "../../shared/group/group.module";
import {ManagementPortalSharedPermissionModule} from "../../shared/permission/permissions.module";

const ENTITY_STATES = [
    ...projectRoute,
    ...projectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSourceModule,
        ManagementPortalSharedSubjectModule,
        ManagementPortalSharedGroupModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        ManagementPortalSharedPermissionModule,
    ],
    declarations: [
        ProjectComponent,
        ProjectDetailComponent,
        ProjectDialogComponent,
        ProjectDeleteDialogComponent,
        ProjectPopupComponent,
        ProjectDeletePopupComponent,
    ],
    entryComponents: [
        ProjectComponent,
        ProjectDialogComponent,
        ProjectPopupComponent,
        ProjectDeleteDialogComponent,
        ProjectDeletePopupComponent,
    ],
    exports: [
        ProjectComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalProjectModule {
}
