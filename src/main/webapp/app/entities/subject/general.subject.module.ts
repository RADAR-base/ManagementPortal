import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {QrCodeModule} from 'ng-qrcode';
import {ManagementPortalSharedModule} from '../../shared';
import {ManagementPortalSharedSubjectModule} from '../../shared/subject/subject.module';
import {GeneralSubjectComponent} from './general.subject.component';
import {subjectPopupRoute, subjectRoute} from './general.subject.route';

const ENTITY_STATES = [
    ...subjectRoute,
    ...subjectPopupRoute,
];

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        ManagementPortalSharedSubjectModule,
        RouterModule.forRoot(ENTITY_STATES, {useHash: true}),
        QrCodeModule,
    ],
    declarations: [
        GeneralSubjectComponent,
    ],
    entryComponents: [
        GeneralSubjectComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [],
})
export class ManagementPortalSubjectModule {
}
