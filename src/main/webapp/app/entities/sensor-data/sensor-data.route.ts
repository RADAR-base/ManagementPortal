import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SensorDataComponent } from './sensor-data.component';
import { SensorDataDetailComponent } from './sensor-data-detail.component';
import { SensorDataPopupComponent } from './sensor-data-dialog.component';
import { SensorDataDeletePopupComponent } from './sensor-data-delete-dialog.component';

import { Principal } from '../../shared';
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const sensorDataRoute: Routes = [
  {
    path: 'sensor-data',
    component: SensorDataComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'sensor-data/:id',
    component: SensorDataDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sensorDataPopupRoute: Routes = [
  {
    path: 'sensor-data-new',
    component: SensorDataPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'sensor-data/:id/edit',
    component: SensorDataPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'sensor-data/:id/delete',
    component: SensorDataDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
