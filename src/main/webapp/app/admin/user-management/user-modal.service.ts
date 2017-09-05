import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { User, UserService } from '../../shared';
import {ADMIN_AUTHORITY} from "../../shared/constants/common.constants";

@Injectable()
export class UserModalService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private userService: UserService
    ) {}

    open(component: Component, login?: string , admin?: boolean, ): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if(admin) {
            let user = new User();
            user.authorities=[ADMIN_AUTHORITY];
            return this.userModalRef(component , user , true);
        }

        if (login) {
            this.userService.find(login)
            .subscribe((user) => {
                if(user.authorities.indexOf(ADMIN_AUTHORITY)>-1) {
                  return this.userModalRef(component, user , true);
                }
                return this.userModalRef(component, user , false);
            });
        } else {
            return this.userModalRef(component, new User() , false);
        }
    }

    userModalRef(component: Component, user: User , isAdmin: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.user = user;
        modalRef.componentInstance.isAdmin = isAdmin;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        });
        return modalRef;
    }
}
