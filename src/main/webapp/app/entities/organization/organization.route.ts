import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import {ORGANIZATION_ADMIN, SYSTEM_ADMIN} from '../../shared/constants/common.constants';
import { OrganizationComponent } from './organization.component';
import { OrganizationDeletePopupComponent } from './organization-delete-dialog.component';
import { OrganizationDetailComponent } from './organization-detail.component';
import { OrganizationPopupComponent } from './organization-dialog.component';

import { ResolvePagingParams } from '../../shared/commons';

export const organizationRoute: Routes = [
    {
        path: 'organization',
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        component: OrganizationComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.organization.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
    {
        path: 'organization/:organizationName',
        component: OrganizationDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN],
            pageTitle: 'managementPortalApp.organization.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const organizationPopupRoute: Routes = [
    {
        path: 'organization-new',
        component: OrganizationPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.organization.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'organization/:organizationName/edit',
        component: OrganizationPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN],
            pageTitle: 'managementPortalApp.organization.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    // {
    //     path: 'organization/:organizationName/delete',
    //     component: OrganizationDeletePopupComponent,
    //     data: {
    //         authorities: [SYSTEM_ADMIN],
    //         pageTitle: 'managementPortalApp.organization.home.title',
    //     },
    //     canActivate: [UserRouteAccessService],
    //     outlet: 'popup',
    // },
];
