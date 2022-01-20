import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SYSTEM_ADMIN } from '../../shared/constants/common.constants';

import { GeneralSubjectComponent } from './general.subject.component';
import { ResolvePagingParams } from '../../shared/commons';
import { SubjectPopupComponent } from '../../shared/subject';

export const subjectRoute: Routes = [
    {
        path: 'subject',
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
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
        component: SubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'general-subject/:login/edit',
        component: SubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
