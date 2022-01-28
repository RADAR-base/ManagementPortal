import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { User, UserService } from '../../shared';
import { SYSTEM_ADMIN } from '../../shared/constants/common.constants';
import { Role } from './role.model';

@Injectable({ providedIn: 'root' })
export class UserModalService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private userService: UserService,
    ) {
    }

    open(component: any, login?: string, admin?: boolean): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (admin) {
            const user = new User();
            user.authorities = [SYSTEM_ADMIN];
            const adminRole = new Role();
            adminRole.authorityName = SYSTEM_ADMIN;
            user.roles = [adminRole];
            return this.userModalRef(component, user, true);
        }

        if (login) {
            this.userService.find(login)
            .subscribe((user) => {
                if (user.authorities.indexOf(SYSTEM_ADMIN) > -1) {
                    return this.userModalRef(component, user, true);
                }
                return this.userModalRef(component, user, false);
            });
        } else {
            return this.userModalRef(component, new User(), false);
        }
    }

    userModalRef(component: any, user: User, isAdmin: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.user = user;
        modalRef.componentInstance.isAdmin = isAdmin;
        modalRef.result.then(() => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
