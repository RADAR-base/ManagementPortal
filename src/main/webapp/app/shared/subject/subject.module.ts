import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QRCodeModule } from 'angular2-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SubjectService,
    SubjectPopupService,
    SubjectComponent,
    SubjectDetailComponent,
    SubjectDialogComponent,
    SubjectPopupComponent,
    SubjectDeletePopupComponent,
    SubjectDeleteDialogComponent,
    SubjectPairPopupComponent,
    SubjectPairDialogComponent,
    subjectRoute,
    subjectPopupRoute,
} from './';

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
        SubjectDialogComponent,
        SubjectDeleteDialogComponent,
        SubjectPairDialogComponent,
        SubjectPopupComponent,
        SubjectDeletePopupComponent,
        SubjectPairPopupComponent
    ],
    entryComponents: [
        SubjectComponent,
        SubjectDialogComponent,
        SubjectPopupComponent,
        SubjectDeleteDialogComponent,
        SubjectDeletePopupComponent,
        SubjectPairDialogComponent,
        SubjectPairPopupComponent
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
