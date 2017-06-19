import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SourceComponent } from './source.component';
import { SourceDetailComponent } from './source-detail.component';
import { SourcePopupComponent } from './source-dialog.component';
import { SourceDeletePopupComponent } from './source-delete-dialog.component';

import { Principal } from '../../shared';

export const sourceRoute: Routes = [
  {
    path: 'source',
    component: SourceComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'source/:id',
    component: SourceDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const sourcePopupRoute: Routes = [
  {
    path: 'source-new',
    component: SourcePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source/:id/edit',
    component: SourcePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'source/:id/delete',
    component: SourceDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.source.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
