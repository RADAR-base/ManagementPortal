import { Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SubjectDetailComponent } from './subject-detail.component';
import { SubjectPopupComponent } from './subject-dialog.component';
import { SubjectDeletePopupComponent } from './subject-delete-dialog.component';
import { SubjectPairPopupComponent } from './subject-pair-dialog.component';

import {PROJECT_ADMIN, SYSTEM_ADMIN} from "../constants/common.constants";
import {SubjectSourceAssignerPopupComponent} from "./source-assigner/source-assigner.component";

export const subjectRoute: Routes = [
  {
    path: 'subject/:id',
    component: SubjectDetailComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
];

export const subjectPopupRoute: Routes = [
  {
    path: 'project-subject-new/:projectId',
    component: SubjectPopupComponent,
    data: {
        authorities:  [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'project-subject/:projectId/:id/edit',
    component: SubjectPopupComponent,
    data: {
        authorities:  [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/delete',
    component: SubjectDeletePopupComponent,
    data: {
        authorities:  [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/pairApp',
    component: SubjectPairPopupComponent,
    data: {
    authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
    pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/sources',
    component: SubjectSourceAssignerPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  },
  {
    path: 'subject/:id/discontinue',
    component: SubjectDeletePopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN, PROJECT_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
