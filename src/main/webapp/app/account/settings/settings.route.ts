import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PROJECT_ADMIN, SYSTEM_ADMIN, ORGANIZATION_ADMIN } from '../../shared/constants/common.constants';
import { SettingsComponent } from './settings.component';

export const settingsRoute: Route = {
    path: 'settings',
    component: SettingsComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN, ORGANIZATION_ADMIN],
        pageTitle: 'global.menu.account.settings',
    },
    canActivate: [UserRouteAccessService],
};
