import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Role } from '../../admin/user-management/role.model';
import { RoleService } from './role.service';

@Injectable()
export class RolePopupService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private roleService: RoleService,
    ) {
    }

    open(component: Component, projectName?: string, authorityName?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (projectName && authorityName) {
            this.roleService.find(projectName, authorityName).subscribe((role) => {
                this.roleModalRef(component, role);
            });
        } else {
            return this.roleModalRef(component, new Role());
        }
    }

    roleModalRef(component: Component, role: Role): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.role = role;
        modalRef.result.then((result) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
