import { Routes} from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import { SourceDataComponent } from './source-data.component';
import { SourceDataDetailComponent } from './source-data-detail.component';
import { SourceDataPopupComponent } from './source-data-dialog.component';
import { SourceDataDeletePopupComponent } from './source-data-delete-dialog.component';

import {PROJECT_ADMIN, SYSTEM_ADMIN} from '../../shared/constants/common.constants';
import {ResolvePagingParams} from '../../shared/commons';

export const sourceDataRoute: Routes = [
  {
    path: 'source-data',
    component: SourceDataComponent,
    resolve: {
      'pagingParams': ResolvePagingParams
    },
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceData.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'source-data/:sourceDataName',
    component: SourceDataDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceData.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sourceDataPopupRoute: Routes = [
  {
    path: 'source-data-new',
    component: SourceDataPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source-data/:sourceDataName/edit',
    component: SourceDataPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source-data/:sourceDataName/delete',
    component: SourceDataDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
