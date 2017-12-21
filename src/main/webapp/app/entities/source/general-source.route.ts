import { Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";
import {GeneralSourceComponent} from "./general-source.component";
import {GeneralSourcePopupComponent} from "./general-source-dialog.component";
import {ResolvePagingParams} from "../../shared/commons";

export const sourceRoute: Routes = [
  {
    path: 'source',
    resolve: {
        'pagingParams': ResolvePagingParams
    },
    component: GeneralSourceComponent,
    data: {
        authorities: [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sourcePopupRoute: Routes = [
  {
    path: 'general-source-new',
    component: GeneralSourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'general-source/:sourceName/edit',
    component: GeneralSourcePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
