import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject, SubjectService } from '../../shared/subject';

@Injectable()
export class GeneralSubjectPopupService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private subjectService: SubjectService,
    ) {
    }

    open(component: any, login?: string, isDelete?: boolean): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (login) {
            this.subjectService.find(login).subscribe((subject: Subject) => {
                this.subjectModalRef(component, subject, isDelete);
            });
        } else {
            return this.subjectModalRef(component, new Subject(), isDelete);
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
