import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SourceTypeComponent } from './source-type.component';
import { SourceTypeDetailComponent } from './source-type-detail.component';
import { SourceTypePopupComponent } from './source-type-dialog.component';
import { SourceTypeDeletePopupComponent } from './source-type-delete-dialog.component';

import { Principal } from '../../shared';
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const sourceTypeRoute: Routes = [
  {
    path: 'source-type',
    component: SourceTypeComponent,
    data: {
        authorities: [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.sourceType.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion',
    component: SourceTypeDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceType.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sourceTypePopupRoute: Routes = [
  {
    path: 'source-type-new',
    component: SourceTypePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion/edit',
    component: SourceTypePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN , PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sourceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source-type/:sourceTypeProducer/:sourceTypeModel/:catalogVersion/delete',
    component: SourceTypeDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN ],
        pageTitle: 'managementPortalApp.sourceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
