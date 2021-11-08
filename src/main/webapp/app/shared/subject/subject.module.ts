import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QrCodeModule } from 'ng-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {
    SubjectComponent,
    SubjectDeleteDialogComponent,
    SubjectDeletePopupComponent,
    SubjectDetailComponent,
    SubjectDialogComponent,
    SubjectPairDialogComponent,
    SubjectPairPopupComponent,
    SubjectPopupComponent,
    subjectPopupRoute,
    SubjectRevisionComponent,
    SubjectRevisionListComponent,
    subjectRoute,
} from './';
import {
    AddSubjectsToGroupDialogComponent
} from "./add-subjects-to-group-dialog.component";
import {
    SubjectSourceAssignerDialogComponent,
    SubjectSourceAssignerPopupComponent,
} from './source-assigner/source-assigner.component';

const ENTITY_STATES = [
    ...subjectRoute,
    ...subjectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        QrCodeModule,
    ],
    declarations: [
        AddSubjectsToGroupDialogComponent,
        SubjectComponent,
        SubjectDetailComponent,
        SubjectRevisionComponent,
        SubjectRevisionListComponent,
        SubjectDialogComponent,
        SubjectDeleteDialogComponent,
        SubjectPairDialogComponent,
        SubjectPopupComponent,
        SubjectDeletePopupComponent,
        SubjectPairPopupComponent,
        SubjectSourceAssignerDialogComponent,
        SubjectSourceAssignerPopupComponent,
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
        SubjectSourceAssignerPopupComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        SubjectComponent,
    ],
})
export class ManagementPortalSharedSubjectModule {
}
