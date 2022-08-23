import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PROJECT_ADMIN, SYSTEM_ADMIN, ORGANIZATION_ADMIN} from '../../shared/constants/common.constants';
import { PasswordComponent } from './password.component';

export const passwordRoute: Route = {
    path: 'password',
    component: PasswordComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN, ORGANIZATION_ADMIN],
        pageTitle: 'global.menu.account.password',
    },
    canActivate: [UserRouteAccessService],
};
