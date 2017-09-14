import {  Routes} from '@angular/router';

import { UserRouteAccessService } from '../../shared';

import {GeneralSubjectComponent} from "./general.subject.component";
import {GeneralSubjectPopupComponent} from "./general.subject-dialog.component";
import {SYSTEM_ADMIN} from "../../shared/constants/common.constants";

export const subjectRoute: Routes = [
  {
    path: 'subject',
    component: GeneralSubjectComponent,
    data: {
        authorities: ['ROLE_SYS_ADMIN'],
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
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.subject.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
    path: 'general-subject/:id/edit',
    component: GeneralSubjectPopupComponent,
    data: {
        authorities: [SYSTEM_ADMIN],
        pageTitle: 'managementPortalApp.subject.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
