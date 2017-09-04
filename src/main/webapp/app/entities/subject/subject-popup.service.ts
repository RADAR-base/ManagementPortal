import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
@Injectable()
export class SubjectPopupService {
    private isOpen = false;
    login ?= 'xxxyxxxx';
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
            var subject = new Subject();
            subject.login = this.login.replace(/[xy]/g, function(c) {
                var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
                return v.toString(16);
            });
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
