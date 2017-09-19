import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PasswordComponent } from './password.component';
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const passwordRoute: Route = {
  path: 'password',
  component: PasswordComponent,
  data: {
    authorities: [SYSTEM_ADMIN, PROJECT_ADMIN ,'ROLE_USER'],
    pageTitle: 'global.menu.account.password'
  },
  canActivate: [UserRouteAccessService]
};
