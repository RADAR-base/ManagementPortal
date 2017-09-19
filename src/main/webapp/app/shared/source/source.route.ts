import { Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import { SourceDetailComponent } from './source-detail.component';
import { SourcePopupComponent } from './source-dialog.component';
import { SourceDeletePopupComponent } from './source-delete-dialog.component';

import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const sourceRoute: Routes = [
  {
    path: 'source/:id',
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
    path: 'project-source-new/:projectId',
    component: SourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'project-source/:projectId/:id/edit',
    component: SourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source/:id/delete',
    component: SourceDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
