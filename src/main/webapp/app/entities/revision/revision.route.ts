import { Routes } from '@angular/router';
import { UserRouteAccessService } from '../../shared';

import { PROJECT_ADMIN, SYSTEM_ADMIN } from '../../shared/constants/common.constants';

import { RevisionComponent } from './revision.component';
import { ResolvePagingParams } from '../../shared/commons';

export const revisionRoute: Routes = [
    {
        path: 'revisions',
        component: RevisionComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'revisions.title',
        },
        canActivate: [UserRouteAccessService],
    },
];
