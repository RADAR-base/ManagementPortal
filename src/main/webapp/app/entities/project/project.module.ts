import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ManagementPortalSharedModule } from '../../shared';
import {
    ProjectService,
    ProjectPopupService,
    ProjectComponent,
    ProjectDetailComponent,
    ProjectDialogComponent,
    ProjectPopupComponent,
    ProjectDeletePopupComponent,
    ProjectDeleteDialogComponent,
    projectRoute,
    projectPopupRoute,
} from './';
import {ManagementPortalSourceModule} from "../source/source.module";
import {ManagementPortalSubjectModule} from "../subject/subject.module";

const ENTITY_STATES = [
    ...projectRoute,
    ...projectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSourceModule,
        ManagementPortalSubjectModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
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
    providers: [
        ProjectService,
        ProjectPopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalProjectModule {}
