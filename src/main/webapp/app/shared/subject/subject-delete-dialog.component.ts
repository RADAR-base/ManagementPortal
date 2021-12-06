import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../util/event-manager.service';
import { SubjectPopupService } from './subject-popup.service';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';

@Component({
    selector: 'jhi-subject-delete-dialog',
    templateUrl: './subject-delete-dialog.component.html',
})
export class SubjectDeleteDialogComponent {
    subject: Subject;
    isDelete: boolean;

    constructor(
            private subjectService: SubjectService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(login: string) {
        if (this.isDelete) {
            this.subjectService.delete(login).subscribe(() => {
                this.activeModal.dismiss(true);
                window.history.back();
            });
        } else {
            this.subjectService.discontinue(this.subject).subscribe(() => {
                this.eventManager.broadcast({name: 'subjectListModification', content: {op: 'DELETE', subject: this.subject}});
                this.activeModal.dismiss(true);
                window.history.back();
            });
        }
    }
}

@Component({
    selector: 'jhi-subject-delete-popup',
    template: '',
})
export class SubjectDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private subjectPopupService: SubjectPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.route.url.subscribe(url => {
                if ('discontinue' === url[2].path) {
                    this.modalRef = this.subjectPopupService
                            .open(SubjectDeleteDialogComponent, params['login'], false);
                } else if ('delete' === url[2].path) {
                    this.modalRef = this.subjectPopupService
                            .open(SubjectDeleteDialogComponent, params['login'], true);
                }
            });
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
