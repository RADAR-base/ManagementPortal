import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import {ORGANIZATION_ADMIN, PROJECT_ADMIN, SYSTEM_ADMIN} from '../constants/common.constants';
import { SubjectSourceAssignerPopupComponent } from './source-assigner/source-assigner.component';
import { SubjectDeletePopupComponent } from './subject-delete-dialog.component';
import { SubjectDetailComponent } from './subject-detail.component';
import { SubjectPopupComponent } from './subject-dialog.component';
import { SubjectPairPopupComponent } from './subject-pair-dialog.component';
import { SubjectRevisionListComponent } from './subject-revision-list.component';
import { SubjectRevisionComponent } from './subject-revision.component';
import { ResolvePagingParams } from '../commons';
import { SubjectDataViewerPopupComponent } from './data-viewer/data-viewer.component';

import {DataSummaryComponent} from "./data-summary/data-summary.component";

export const subjectRoute: Routes = [
    {
        path: 'subject/:login',
        component: SubjectDetailComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
        {
            path: 'subject/data-summary/:login',
            component: DataSummaryComponent,
            data: {
                authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
                pageTitle: 'managementPortalApp.subject.home.title',
            },
            canActivate: [UserRouteAccessService],
        },
    {
        path: 'subject/:login/revisions',
        component: SubjectRevisionListComponent,
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
    {
        path: 'subject/:login/revisions/:revisionNb',
        component: SubjectRevisionComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const subjectPopupRoute: Routes = [
    {
        path: 'project-subject-new/:projectName',
        component: SubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'project-subject/:projectName/:login/edit',
        component: SubjectPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'subject/:login/delete',
        component: SubjectDeletePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'subject/:login/pairApp',
        component: SubjectPairPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'subject/:login/sources',
        component: SubjectSourceAssignerPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
        {
            path: 'subject/:login/dataViewer',
            component: SubjectDataViewerPopupComponent,
            data: {
                authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
                pageTitle: 'managementPortalApp.subject.home.title',
            },
            canActivate: [UserRouteAccessService],
            outlet: 'popup',
        },

    {
        path: 'subject/:login/discontinue',
        component: SubjectDeletePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
