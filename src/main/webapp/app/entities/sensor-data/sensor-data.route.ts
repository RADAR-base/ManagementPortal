import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SensorDataComponent } from './sensor-data.component';
import { SensorDataDetailComponent } from './sensor-data-detail.component';
import { SensorDataPopupComponent } from './sensor-data-dialog.component';
import { SensorDataDeletePopupComponent } from './sensor-data-delete-dialog.component';

import { Principal } from '../../shared';

export const sensorDataRoute: Routes = [
  {
    path: 'sensor-data',
    component: SensorDataComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'sensor-data/:id',
    component: SensorDataDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
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
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'sensor-data/:id/edit',
    component: SensorDataPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'sensor-data/:id/delete',
    component: SensorDataDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.sensorData.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
