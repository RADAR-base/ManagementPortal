import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { SubjectComponent} from './subject.component';
import { SubjectDetailComponent } from './subject-detail.component';
import { SubjectPopupComponent } from './subject-dialog.component';
import { SubjectDeletePopupComponent } from './subject-delete-dialog.component';
import { SubjectPairPopupComponent } from './subject-pair-dialog.component';

import { Principal } from '../../shared';

export const subjectRoute: Routes = [
  // {
  //   path: 'subject',
  //   component: SubjectComponent,
  //   data: {
  //       authorities: ['ROLE_USER'],
  //       pageTitle: 'managementPortalApp.subject.home.title'
  //   },
  //   canActivate: [UserRouteAccessService]
  // },
  {
    path: 'subject/:id',
    component: SubjectDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
];

export const subjectPopupRoute: Routes = [
  {
    path: 'subject-new',
    component: SubjectPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/edit',
    component: SubjectPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/delete',
    component: SubjectDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/pairApp',
    component: SubjectPairPopupComponent,
    data: {
    authorities: ['ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'],
    pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/discontinue',
    component: SubjectDeletePopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
