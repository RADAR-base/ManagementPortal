import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

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
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        SubjectComponent,
        SubjectDetailComponent,
        SubjectDialogComponent,
        SubjectDeleteDialogComponent,
        SubjectPopupComponent,
        SubjectDeletePopupComponent,
    ],
    entryComponents: [
        SubjectComponent,
        SubjectDialogComponent,
        SubjectPopupComponent,
        SubjectDeleteDialogComponent,
        SubjectDeletePopupComponent,
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
export class ManagementPortalSubjectModule {}
