import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PROJECT_ADMIN, SYSTEM_ADMIN, ORGANIZATION_ADMIN } from '../../shared/constants/common.constants';
import { QueriesComponent } from './queries.component';

export const queriesRoute: Route = {
    path: 'queries',
    component: QueriesComponent,
    data: {
        authorities: [],
        pageTitle: 'global.menu.queries',
    },
};
