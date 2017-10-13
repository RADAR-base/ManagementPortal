import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { DatePipe } from '@angular/common';
import { Project } from './project.model';
import { ProjectService } from './project.service';
@Injectable()
export class ProjectPopupService {
    private isOpen = false;
    constructor(
        private datePipe: DatePipe,
        private modalService: NgbModal,
        private router: Router,
        private projectService: ProjectService

    ) {}

    open(component: Component, projectName?: string | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (projectName) {
            this.projectService.find(projectName).subscribe((project) => {
                project.startDate = this.datePipe
                    .transform(project.startDate, 'yyyy-MM-ddThh:mm');
                project.endDate = this.datePipe
                    .transform(project.endDate, 'yyyy-MM-ddThh:mm');
                this.projectModalRef(component, project);
            });
        } else {
            return this.projectModalRef(component, new Project());
        }
    }

    projectModalRef(component: Component, project: Project): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.project = project;
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
