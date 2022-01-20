import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { copyProject, Project, ProjectService } from '../../shared';
import { Observable, of, throwError } from 'rxjs';
import { first, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProjectPopupService {
    private isOpen = false;

    constructor(
            private datePipe: DatePipe,
            private modalService: NgbModal,
            private router: Router,
            private projectService: ProjectService,
    ) {
    }

    open(component: any, organizationName?: string, projectName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (projectName) {
            return this.projectService.find(projectName).pipe(
                map((project) => {
                    project.startDate = this.datePipe.transform(project.startDate, 'yyyy-MM-ddThh:mm');
                    project.endDate = this.datePipe.transform(project.endDate, 'yyyy-MM-ddThh:mm');
                    return this.projectModalRef(component, organizationName, project);
                }),
                first(),
            );
        } else if (organizationName) {
            return of(this.projectModalRef(component, organizationName, {}));
        } else {
            return throwError("Cannot open project dialog without project or organization");
        }
    }

    projectModalRef(component: any, organizationName: string, project: Project): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.project = copyProject(project);
        modalRef.componentInstance.organizationName = organizationName;
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
