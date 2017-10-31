import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { QRCodeModule } from 'angular2-qrcode';

import { ManagementPortalSharedModule } from '../../shared';
import {ManagementPortalSharedSubjectModule} from "../../shared/subject/subject.module";
import {GeneralSubjectComponent} from "./general.subject.component";
import {subjectRoute, subjectPopupRoute} from "./general.subject.route";
import {
    GeneralSubjectDialogComponent,
    GeneralSubjectPopupComponent
} from "./general.subject-dialog.component";
import {GeneralSubjectPopupService} from "./general.subject-popup.service";


const ENTITY_STATES = [
    ...subjectRoute,
    ...subjectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSubjectModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true }),
        QRCodeModule
    ],
    declarations: [
        GeneralSubjectComponent,
        GeneralSubjectDialogComponent,
        GeneralSubjectPopupComponent
    ],
    entryComponents: [
        GeneralSubjectComponent,
        GeneralSubjectPopupComponent,
        GeneralSubjectDialogComponent
    ],
    providers: [
        GeneralSubjectPopupService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [

    ],
})
export class ManagementPortalSubjectModule {}
