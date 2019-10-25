import { Injectable } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivate,
    Resolve,
    RouterStateSnapshot,
    Routes,
} from '@angular/router';

import { PaginationUtil } from 'ng-jhipster';

import { Principal } from '../../shared';
import { UserDeleteDialogComponent } from './user-management-delete-dialog.component';
import { UserMgmtDetailComponent } from './user-management-detail.component';
import { UserDialogComponent } from './user-management-dialog.component';

import { UserMgmtComponent } from './user-management.component';
import { UserSendActivationLinkComponent } from './user-mgnt-send-activation.component';
import { SYSTEM_ADMIN} from "../../shared/constants/common.constants";

@Injectable()
export class UserResolve implements CanActivate {

    constructor(private principal: Principal) {
    }

    canActivate() {
        return this.principal.identity()
        .then(() => this.principal.hasAnyAuthority(['ROLE_SYS_ADMIN']));
    }
}

@Injectable()
export class UserResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: PaginationUtil) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        return {
            page: this.paginationUtil.parsePage(page),
            predicate: this.paginationUtil.parsePredicate(sort),
            ascending: this.paginationUtil.parseAscending(sort),
        };
    }
}

export const userMgmtRoute: Routes = [
    {
        path: 'user-management',
        component: UserMgmtComponent,
        resolve: {
            'pagingParams': UserResolvePagingParams,
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
