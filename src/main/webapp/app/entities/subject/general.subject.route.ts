import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SYSTEM_ADMIN } from '../../shared/constants/common.constants';
import { GeneralSubjectPopupComponent } from './general.subject-dialog.component';

import { GeneralSubjectComponent } from './general.subject.component';
import { ResolvePagingParams } from '../../shared/commons';

export const subjectRoute: Routes = [
    {
        path: 'subject',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        component: GeneralSubjectComponent,
        data: {
            authorities: ['ROLE_SYS_ADMIN'],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const subjectPopupRoute: Routes = [
    {
        path: 'general-subject-new',
        component: GeneralSubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'general-subject/:login/edit',
        component: GeneralSubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
