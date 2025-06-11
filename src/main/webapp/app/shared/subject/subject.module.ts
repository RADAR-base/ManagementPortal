import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QrCodeModule } from 'ng-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {
    FilterBadgeComponent,
    LoadMoreComponent,
    NgbDatePipe,
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
import { AddSubjectsToGroupDialogComponent } from './add-subjects-to-group-dialog.component';
import {
    SubjectSourceAssignerDialogComponent,
    SubjectSourceAssignerPopupComponent,
} from './source-assigner/source-assigner.component';

import {
    SubjectDataViewerPopupComponent,
    SubjectDataViewerDialogComponent,
} from './data-viewer/data-viewer.component';
import { QueryViewerComponent } from './query-viewer/query-viewer.component';

import {
    QueryEvaluationPopupComponent,
    QueryEvaluationDialogComponent,
} from './query-evaluation/query-evaluation.component';

import { DeleteQueryConfirmDialogComponent } from './query-viewer/delete-query-confirm-dialog.component';

const ENTITY_STATES = [...subjectRoute, ...subjectPopupRoute];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true }),
        QrCodeModule,
    ],
    declarations: [
        AddSubjectsToGroupDialogComponent,
        FilterBadgeComponent,
        LoadMoreComponent,
        NgbDatePipe,
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
        QueryEvaluationPopupComponent,
        QueryEvaluationDialogComponent,
        SubjectDataViewerPopupComponent,
        SubjectDataViewerDialogComponent,
        QueryViewerComponent,
        DeleteQueryConfirmDialogComponent,
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
        QueryEvaluationPopupComponent,
        QueryEvaluationDialogComponent,
        SubjectDataViewerPopupComponent,
        SubjectDataViewerDialogComponent,
        DeleteQueryConfirmDialogComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [SubjectComponent, LoadMoreComponent],
})
export class ManagementPortalSharedSubjectModule {}
