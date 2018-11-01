import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../shared';
import { PROJECT_ADMIN, SYSTEM_ADMIN } from '../shared/constants/common.constants';

import {
    auditsRoute,
    configurationRoute,
    docsRoute,
    healthRoute,
    logsRoute,
    metricsRoute,
    userDialogRoute,
    userMgmtRoute,
} from './';

const ADMIN_ROUTES = [
    auditsRoute,
    configurationRoute,
    docsRoute,
    healthRoute,
    logsRoute,
    ...userMgmtRoute,
    metricsRoute,
];

export const adminState: Routes = [{
    path: '',
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
    },
    canActivate: [UserRouteAccessService],
    children: ADMIN_ROUTES,
},
    ...userDialogRoute,
];
