import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { ProjectService } from '../project/project.service';
import { Subject, SubjectStatus } from './subject.model';
import { SubjectService } from './subject.service';
import { Observable, of } from 'rxjs';
import { first, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
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

    open(component: any, login?: string, isDelete?: boolean, projectName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (login) {
            return this.subjectService.find(login).pipe(
                map((subject: Subject) => this.subjectModalRef(component, subject, isDelete)),
                first(),
            );
        } else if (projectName) {
            return this.projectService.find(projectName).pipe(
                map((project) => this.subjectModalRef(component, {
                    project,
                    sources: [],
                    status: SubjectStatus.ACTIVATED,
                }, isDelete)),
                first(),
            );
        } else {
            return of(this.subjectModalRef(component, {
                sources: [],
                status: SubjectStatus.ACTIVATED,
            }, isDelete));
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
