import {Routes,} from '@angular/router';

import {UserDeleteDialogComponent} from './user-management-delete-dialog.component';
import {UserMgmtDetailComponent} from './user-management-detail.component';
import {UserDialogComponent} from './user-management-dialog.component';

import {UserMgmtComponent} from './user-management.component';
import {UserSendActivationLinkComponent} from './user-mgnt-send-activation.component';
import {SYSTEM_ADMIN} from '../../shared/constants/common.constants';
import {ResolvePagingParams} from '../../shared/commons';

export const userMgmtRoute: Routes = [
    {
        path: 'user-management',
        component: UserMgmtComponent,
        resolve: {
            'pagingParams': ResolvePagingParams,
        },
        data: {
            pageTitle: 'userManagement.home.title',
            authorities: [SYSTEM_ADMIN],
        },
    },
    {
        path: 'user-management/:login',
        component: UserMgmtDetailComponent,
        data: {
            pageTitle: 'userManagement.home.title',
        },
    },
];

export const userDialogRoute: Routes = [
    {
        path: 'user-management-new',
        component: UserDialogComponent,
        outlet: 'popup',
    },
    {
        path: 'user-management-new-admin',
        component: UserDialogComponent,
        outlet: 'popup',
    },
    {
        path: 'user-management/:login/edit',
        component: UserDialogComponent,
        outlet: 'popup',
    },
    {
        path: 'user-management/:login/delete',
        component: UserDeleteDialogComponent,
        outlet: 'popup',
    },
    {
        path: 'user-management/:login/send-activation',
        component: UserSendActivationLinkComponent,
        outlet: 'popup',
    },
];
