import {Routes} from '@angular/router';

import {UserRouteAccessService} from '../../shared';
import {ORGANIZATION_ADMIN, PROJECT_ADMIN, SYSTEM_ADMIN} from '../../shared/constants/common.constants';

import {RoleDetailComponent} from './role-detail.component';

export const roleRoute: Routes = [
    {
        path: 'role/:projectName/:authorityName',
        component: RoleDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.role.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
    {
        path: 'role/:organizationName/:authorityName',
        component: RoleDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN],
            pageTitle: 'managementPortalApp.role.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];
