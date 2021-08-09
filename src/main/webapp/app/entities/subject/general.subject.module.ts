import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QRCodeModule } from 'angularx-qrcode';
import { ManagementPortalSharedModule } from '../../shared';
import { ManagementPortalSharedSubjectModule } from '../../shared/subject/subject.module';
import {
    GeneralSubjectDialogComponent,
    GeneralSubjectPopupComponent,
} from './general.subject-dialog.component';
import { GeneralSubjectComponent } from './general.subject.component';
import { subjectPopupRoute, subjectRoute } from './general.subject.route';

const ENTITY_STATES = [
    ...subjectRoute,
    ...subjectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSubjectModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        QRCodeModule,
    ],
    declarations: [
        GeneralSubjectComponent,
        GeneralSubjectDialogComponent,
        GeneralSubjectPopupComponent,
    ],
    entryComponents: [
        GeneralSubjectComponent,
        GeneralSubjectPopupComponent,
        GeneralSubjectDialogComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [],
})
export class ManagementPortalSubjectModule {
}
