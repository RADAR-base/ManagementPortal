import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { DeviceTypeComponent } from './device-type.component';
import { DeviceTypeDetailComponent } from './device-type-detail.component';
import { DeviceTypePopupComponent } from './device-type-dialog.component';
import { DeviceTypeDeletePopupComponent } from './device-type-delete-dialog.component';

import { Principal } from '../../shared';

export const deviceTypeRoute: Routes = [
  {
    path: 'device-type',
    component: DeviceTypeComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.deviceType.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'device-type/:id',
    component: DeviceTypeDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.deviceType.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const deviceTypePopupRoute: Routes = [
  {
    path: 'device-type-new',
    component: DeviceTypePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.deviceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'device-type/:id/edit',
    component: DeviceTypePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.deviceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'device-type/:id/delete',
    component: DeviceTypeDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.deviceType.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
