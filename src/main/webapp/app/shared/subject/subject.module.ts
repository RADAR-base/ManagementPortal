import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QRCodeModule } from 'angular2-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SubjectService,
    SubjectPopupService,
    SubjectComponent,
    SubjectDetailComponent,
    SubjectRevisionComponent,
    SubjectDialogComponent,
    SubjectPopupComponent,
    SubjectDeletePopupComponent,
    SubjectDeleteDialogComponent,
    SubjectPairPopupComponent,
    SubjectPairDialogComponent,
    subjectRoute,
    subjectPopupRoute,
} from './';
import {
    SubjectSourceAssignerDialogComponent,
    SubjectSourceAssignerPopupComponent
} from "./source-assigner/source-assigner.component";

const ENTITY_STATES = [
    ...subjectRoute,
    ...subjectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true }),
        QRCodeModule
    ],
    declarations: [
        SubjectComponent,
        SubjectDetailComponent,
        SubjectRevisionComponent,
        SubjectDialogComponent,
        SubjectDeleteDialogComponent,
        SubjectPairDialogComponent,
        SubjectPopupComponent,
        SubjectDeletePopupComponent,
        SubjectPairPopupComponent,
        SubjectSourceAssignerDialogComponent,
        SubjectSourceAssignerPopupComponent
    ],
    entryComponents: [
        SubjectComponent,
        SubjectDialogComponent,
        SubjectPopupComponent,
        SubjectDeleteDialogComponent,
        SubjectDeletePopupComponent,
        SubjectPairDialogComponent,
        SubjectPairPopupComponent,
        SubjectSourceAssignerDialogComponent,
        SubjectSourceAssignerPopupComponent
    ],
    providers: [
        SubjectService,
        SubjectPopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        SubjectComponent
    ],
})
export class ManagementPortalSharedSubjectModule {}
