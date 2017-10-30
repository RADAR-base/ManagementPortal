import { Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import { SourceDetailComponent } from './source-detail.component';
import { SourcePopupComponent } from './source-dialog.component';
import { SourceDeletePopupComponent } from './source-delete-dialog.component';

import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const sourceRoute: Routes = [
  {
    path: 'source/:sourceName',
    component: SourceDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sourcePopupRoute: Routes = [
  {
    path: 'project-source-new/:projectName',
    component: SourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'project-source/:projectName/:sourceName/edit',
    component: SourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source/:sourceName/delete',
    component: SourceDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
