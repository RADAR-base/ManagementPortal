import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { ResolvePagingParams } from '../../shared/commons';

import { PROJECT_ADMIN, SYSTEM_ADMIN } from '../../shared/constants/common.constants';
import { GeneralSourcePopupComponent } from './general-source-dialog.component';
import { GeneralSourceComponent } from './general-source.component';

export const sourceRoute: Routes = [
    {
        path: 'source',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        component: GeneralSourceComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.source.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const sourcePopupRoute: Routes = [
    {
        path: 'general-source-new',
        component: GeneralSourcePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.source.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'general-source/:sourceName/edit',
        component: GeneralSourcePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.source.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
