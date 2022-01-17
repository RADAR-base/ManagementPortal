import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { ProjectService } from '../project';
import { GroupService } from './group.service';
import { Group } from './group.model';
import { Observable, throwError } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class GroupPopupService {
    private isOpen = false;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private groupService: GroupService,
        private projectService: ProjectService,
    ) {}

    open(component: any, id?: number, isDelete?: boolean, projectName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            return this.groupService.find(id, projectName).pipe(
              map((group: Group) => {
                if (group) {
                    group.projectName = projectName;
                }
                return this.groupModalRef(component, group, isDelete);
              }),
            );
        } else if (projectName) {
            return this.projectService.find(projectName).pipe(
              map((project) => {
                const group: Group = {
                    projectId: project.id,
                    projectName: project.projectName,
                };
                return this.groupModalRef(component, group, isDelete);
              }),
            );
        } else {
            return throwError("No group given");
        }
    }

    groupModalRef(component: any, group: Group, isDelete?: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.group = group;
        modalRef.componentInstance.isDelete = isDelete;
        modalRef.result.then(() => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true, queryParamsHandling: 'merge'});
            this.isOpen = false;
        }, () => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true, queryParamsHandling: 'merge'});
            this.isOpen = false;
        });
        return modalRef;
    }
}
