import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { PatientComponent } from './patient.component';
import { PatientDetailComponent } from './patient-detail.component';
import { PatientPopupComponent } from './patient-dialog.component';
import { PatientDeletePopupComponent } from './patient-delete-dialog.component';

import { Principal } from '../../shared';

export const patientRoute: Routes = [
  {
    path: 'patient',
    component: PatientComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.patient.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'patient/:id',
    component: PatientDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.patient.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const patientPopupRoute: Routes = [
  {
    path: 'patient-new',
    component: PatientPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.patient.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'patient/:id/edit',
    component: PatientPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.patient.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'patient/:id/delete',
    component: PatientDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.patient.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
