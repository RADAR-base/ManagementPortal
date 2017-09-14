import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SettingsComponent } from './settings.component';
import {SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const settingsRoute: Route = {
  path: 'settings',
  component: SettingsComponent,
  data: {
    authorities: [SYSTEM_ADMIN],
    pageTitle: 'global.menu.account.settings'
  },
  canActivate: [UserRouteAccessService]
};
