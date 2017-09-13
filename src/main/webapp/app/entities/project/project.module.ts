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
import {ProjectSubjectDialogComponent, ProjectSubjectPopupComponent} from "./subject/subject-dialog.component";
import {ProjectSubjectPopupService} from "./subject/subject-popup.service";

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
        ProjectSubjectDialogComponent,
        ProjectSubjectPopupComponent
    ],
    entryComponents: [
        ProjectComponent,
        ProjectDialogComponent,
        ProjectPopupComponent,
        ProjectDeleteDialogComponent,
        ProjectDeletePopupComponent,
        ProjectSubjectDialogComponent
    ],
    providers: [
        ProjectService,
        ProjectPopupService,
        ProjectSubjectPopupService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ManagementPortalProjectModule {}
