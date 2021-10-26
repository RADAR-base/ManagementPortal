import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { ProjectService } from '../project';
import {GroupService} from "./group.service";
import {Group} from "./group.model";

@Injectable({ providedIn: 'root' })
export class GroupPopupService {
    private isOpen = false;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private groupService: GroupService,
        private projectService: ProjectService,
    ) {}

    open(component: any, id?: number, isDelete?: boolean, projectName?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.groupService.find(id, projectName).subscribe((group: Group) => {
                if(group){
                    group.projectName = projectName;
                }
                this.groupModalRef(component, group, isDelete);
            });
        } else if (projectName) {
            this.projectService.find(projectName).subscribe((project) => {
                const group = new Group();
                group.projectId = project.id;
                group.projectName = project.projectName;
                return this.groupModalRef(component, group, isDelete);
            });
        }
    }

    groupModalRef(component: any, group: Group, isDelete?: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.group = group;
        modalRef.componentInstance.isDelete = isDelete;
        modalRef.result.then(() => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, () => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
