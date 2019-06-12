import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ProjectService } from '../project/project.service';
import { Subject } from './subject.model';
import { SubjectService } from './subject.service';

@Injectable()
export class SubjectPopupService {
    private isOpen = false;
    login ? = 'xxxyxxxx';

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private subjectService: SubjectService,
            private projectService: ProjectService,
    ) {
    }

    open(component: any, login?: string, isDelete?: boolean, projectName?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (login) {
            this.subjectService.find(login).subscribe((subject) => {
                this.subjectModalRef(component, subject, isDelete);
            });
        } else if (projectName) {
            this.projectService.find(projectName).subscribe((project) => {
                const subject = new Subject();
                subject.project = project;
                return this.subjectModalRef(component, subject, isDelete);
            });
        }
    }

    subjectModalRef(component: any, subject: Subject, isDelete?: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.subject = subject;
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
