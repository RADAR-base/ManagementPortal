import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import {ORGANIZATION_ADMIN, PROJECT_ADMIN, SYSTEM_ADMIN} from '../../shared/constants/common.constants';
import { ProjectComponent } from './project.component';
// import { ProjectDeletePopupComponent } from './project-delete-dialog.component';
import { ProjectDetailComponent } from './project-detail.component';
import { ProjectPopupComponent } from './project-dialog.component';

import { ResolvePagingParams } from '../../shared/commons';

export const projectRoute: Routes = [
    {
        path: 'project',
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        component: ProjectComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.project.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
    {
        path: 'project/:projectName',
        component: ProjectDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.project.home.title',
        },
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        canActivate: [UserRouteAccessService],
    },
];

export const projectPopupRoute: Routes = [
    {
        path: 'organization/:organizationName/project-new',
        component: ProjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN],
            pageTitle: 'managementPortalApp.project.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'organization/:organizationName/project/:projectName/edit',
        component: ProjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.project.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    // {
    //     path: 'organizations/:organizationName/project/:projectName/delete',
    //     component: ProjectDeletePopupComponent,
    //     data: {
    //         authorities: [SYSTEM_ADMIN],
    //         pageTitle: 'managementPortalApp.project.home.title',
    //     },
    //     canActivate: [UserRouteAccessService],
    //     outlet: 'popup',
    // },
];
