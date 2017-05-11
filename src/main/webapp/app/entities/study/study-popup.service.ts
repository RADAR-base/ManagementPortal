import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { DatePipe } from '@angular/common';
import { Study } from './study.model';
import { StudyService } from './study.service';
@Injectable()
export class StudyPopupService {
    private isOpen = false;
    constructor(
        private datePipe: DatePipe,
        private modalService: NgbModal,
        private router: Router,
        private studyService: StudyService

    ) {}

    open(component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.studyService.find(id).subscribe((study) => {
                study.startDate = this.datePipe
                    .transform(study.startDate, 'yyyy-MM-ddThh:mm');
                study.endDate = this.datePipe
                    .transform(study.endDate, 'yyyy-MM-ddThh:mm');
                this.studyModalRef(component, study);
            });
        } else {
            return this.studyModalRef(component, new Study());
        }
    }

    studyModalRef(component: Component, study: Study): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.study = study;
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
