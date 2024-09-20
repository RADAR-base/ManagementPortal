import {Routes} from '@angular/router';
import {UserRouteAccessService} from "../auth/user-route-access-service";
import {GroupPopupComponent} from "./group-dialog.component";
import {GroupDeletePopupComponent} from "./group-delete-dialog.component";
import {ORGANIZATION_ADMIN, PROJECT_ADMIN, SYSTEM_ADMIN} from "../constants/common.constants";

export const groupRoute: Routes = [];

export const groupPopupRoute: Routes = [
    {
        path: 'project-group-new/:projectName',
        component: GroupPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.group.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'project-group/:projectName/:id/delete',
        component: GroupDeletePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN, ORGANIZATION_ADMIN, PROJECT_ADMIN],
            pageTitle: 'managementPortalApp.group.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
