import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SourceDataComponent } from './source-data.component';
import { SourceDataDetailComponent } from './source-data-detail.component';
import { SourceDataPopupComponent } from './source-data-dialog.component';
import { SourceDataDeletePopupComponent } from './source-data-delete-dialog.component';

import { Principal } from '../../shared';
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

@Injectable()
export class SourceDataResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: PaginationUtil) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        return {
            page: this.paginationUtil.parsePage(page),
            predicate: this.paginationUtil.parsePredicate(sort),
            ascending: this.paginationUtil.parseAscending(sort)
        };
    }
}

export const sourceDataRoute: Routes = [
  {
    path: 'source-data',
    component: SourceDataComponent,
    resolve: {
      'pagingParams': SourceDataResolvePagingParams
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
