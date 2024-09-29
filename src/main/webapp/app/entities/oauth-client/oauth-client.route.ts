import {Routes} from '@angular/router';

import {UserRouteAccessService} from '../../shared';
import {SYSTEM_ADMIN} from '../../shared/constants/common.constants';
import {OAuthClientDeletePopupComponent} from './oauth-client-delete-dialog.component';
import {OAuthClientPopupComponent} from './oauth-client-dialog.component';
import {OAuthClientComponent} from './oauth-client.component';

export const oauthClientRoute: Routes = [
    {
        path: 'oauth-client',
        component: OAuthClientComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.oauthClient.home.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

export const oauthClientPopupRoute: Routes = [
    {
        path: 'oauth-client-new',
        component: OAuthClientPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.oauthClient.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'oauth-client/:clientId/edit',
        component: OAuthClientPopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.oauthClient.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
    {
        path: 'oauth-client/:clientId/delete',
        component: OAuthClientDeletePopupComponent,
        data: {
            authorities: [SYSTEM_ADMIN],
            pageTitle: 'managementPortalApp.oauthClient.home.title',
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup',
    },
];
