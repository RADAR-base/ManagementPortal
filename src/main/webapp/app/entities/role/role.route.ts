import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import { RolePopupComponent } from './role-dialog.component';

import {RoleDetailComponent} from "./role-detail.component";

export const roleRoute: Routes = [
  // {
  //   path: 'role',
  //   component: RoleComponent,
  //   data: {
  //       authorities: ['ROLE_USER'],
  //       pageTitle: 'managementPortalApp.role.home.title'
  //   },
  //   canActivate: [UserRouteAccessService]
  // },
  {
    path: 'role/:id',
    component: RoleDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.role.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const rolePopupRoute: Routes = [
  {
    path: 'role-new',
    component: RolePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.role.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'role/:id/edit',
    component: RolePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.role.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
