import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QrCodeModule } from 'ng-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {
    FilterBadgeComponent,
    LoadMoreComponent, NgbDatePipe,
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



import {
          SubjectDataViewerPopupComponent,
            SubjectDataViewerDialogComponent
} from './data-viewer/data-viewer.component';
import { QueryViewerComponent } from './query-viewer/query-viewer.component';
import { DataSummaryComponent } from './data-summary/data-summary.component';
import { SingleRowGraphComponent } from './data-summary/single-row-graph/single-row-graph.component';
import { YOURCOMPONENTNAMEComponent } from './data-summary/yourcomponentname/yourcomponentname.component';
import { DoubleRowGraphComponent } from './data-summary/double-row-graph/double-row-graph.component';




import {
          QueryEvaluationPopupComponent,
            QueryEvaluationDialogComponent
} from './query-evaluation/query-evaluation.component';




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
        DataSummaryComponent,
        SingleRowGraphComponent,
        YOURCOMPONENTNAMEComponent,
        DoubleRowGraphComponent,

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
                SubjectDataViewerDialogComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        SubjectComponent,
        LoadMoreComponent,
    ],
})
export class ManagementPortalSharedSubjectModule {
}
