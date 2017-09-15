import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import {SubjectService} from "../../shared/subject/subject.service";
import {Subject} from "../../shared/subject/subject.model";
@Injectable()
export class GeneralSubjectPopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private subjectService: SubjectService

    ) {}

    open(component: Component, id?: number | any , isDelete?: boolean): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.subjectService.find(id).subscribe((subject) => {
                this.subjectModalRef(component, subject , isDelete);
            });
        } else {
            let subject = new Subject();
            return this.subjectModalRef(component, subject , isDelete);
        }
    }

    subjectModalRef(component: Component, subject: Subject , isDelete?: boolean): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});

        modalRef.componentInstance.subject = subject;
        modalRef.componentInstance.isDelete = isDelete;
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
