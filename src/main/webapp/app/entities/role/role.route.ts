import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import { RolePopupComponent } from './role-dialog.component';

import {RoleDetailComponent} from "./role-detail.component";
import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../../shared/constants/common.constants";

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
    path: 'role/:projectName/:authorityName',
    component: RoleDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
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
        authorities: [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.role.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'role/:projectName/:authorityName/edit',
    component: RolePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.role.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
