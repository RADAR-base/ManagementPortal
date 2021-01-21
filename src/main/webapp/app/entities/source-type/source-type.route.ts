import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PROJECT_ADMIN, SYSTEM_ADMIN } from '../../shared/constants/common.constants';
import { SourceTypeDeletePopupComponent } from './source-type-delete-dialog.component';
import { SourceTypeDetailComponent } from './source-type-detail.component';
import { SourceTypePopupComponent } from './source-type-dialog.component';

import { SourceTypeComponent } from './source-type.component';
import { ResolvePagingParams } from '../../shared/commons';

export const sourceTypeRoute: Routes = [
    {
        path: 'source-type',
        component: SourceTypeComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.sourceType.home.title',
        },
        canActivate: [UserRouteAccessService],
    }, {
        path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion',
        component: SourceTypeDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.sourceType.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const sourceTypePopupRoute: Routes = [
    {
        path: 'source-type-new',
        component: SourceTypePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.sourceType.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion/edit',
        component: SourceTypePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.sourceType.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion/delete',
        component: SourceTypeDeletePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.sourceType.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
