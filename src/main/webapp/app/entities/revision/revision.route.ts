import { Routes } from '@angular/router';
import { UserRouteAccessService } from '../../shared';
import { ResolvePagingParams } from '../../shared/commons';

import { PROJECT_ADMIN, SYSTEM_ADMIN } from '../../shared/constants/common.constants';

import { RevisionComponent } from './revision.component';

export const revisionRoute: Routes = [
    {
        path: 'revisions',
        component: RevisionComponent,
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
