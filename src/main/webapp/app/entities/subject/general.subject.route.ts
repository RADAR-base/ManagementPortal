import {  Routes} from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import {GeneralSubjectComponent} from "./general.subject.component";
import {GeneralSubjectPopupComponent} from "./general.subject-dialog.component";

export const subjectRoute: Routes = [
  {
    path: 'subject',
    component: GeneralSubjectComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
];

export const subjectPopupRoute: Routes = [
    {
        path: 'general-subject-new',
        component: GeneralSubjectPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'managementPortalApp.subject.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
    path: 'general-subject/:id/edit',
    component: GeneralSubjectPopupComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
