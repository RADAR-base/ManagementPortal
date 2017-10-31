import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SettingsComponent } from './settings.component';
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const settingsRoute: Route = {
  path: 'settings',
  component: SettingsComponent,
  data: {
    authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
    pageTitle: 'global.menu.account.settings'
  },
  canActivate: [UserRouteAccessService]
};
