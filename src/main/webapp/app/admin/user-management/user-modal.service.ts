import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { User, UserService } from '../../shared';
import { SYSTEM_ADMIN } from '../../shared/constants/common.constants';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class UserModalService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private userService: UserService,
    ) {
    }

    open(component: any, login?: string, admin?: boolean): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (admin) {
            const user = {
                authorities: [SYSTEM_ADMIN],
                roles: [{
                    authorityName: SYSTEM_ADMIN
                }],
            }
            return of(this.userModalRef(component, user, true));
        }

        if (login) {
            return this.userService.find(login).pipe(
              map((user) => this.userModalRef(component, user, user.authorities.indexOf(SYSTEM_ADMIN) > -1)),
            );
        } else {
            return of(this.userModalRef(component, { authorities: [] }, false));
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
