import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import {Subject} from './subject.model';
import { SubjectPopupService } from './subject-popup.service';
import { SubjectService } from './subject.service';

@Component({
    selector: 'jhi-subject-delete-dialog',
    templateUrl: './subject-delete-dialog.component.html'
})
export class SubjectDeleteDialogComponent {

    subject: Subject;
    isDelete : boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private subjectService: SubjectService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['subject' , 'project' , 'projectStatus']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(login: string) {
        if(this.isDelete) {
            this.subjectService.delete(login).subscribe((response) => {
                this.eventManager.broadcast({
                    name: 'subjectListModification',
                    content: 'Deleted an subject'
                });
                this.activeModal.dismiss(true);
            });
        }
        else {
            this.subjectService.discontinue(this.subject).subscribe((res: Subject) => {
                this.eventManager.broadcast({name: 'subjectListModification', content: 'OK'});
                this.activeModal.dismiss(true);
            });
        }
    }
}

@Component({
    selector: 'jhi-subject-delete-popup',
    template: ''
})
export class SubjectDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.route.url.subscribe(url =>{
                if('discontinue'==(url[2].path)) {
                    this.modalRef = this.subjectPopupService
                    .open(SubjectDeleteDialogComponent, params['login'] , false);
                }
                else if('delete'==(url[2].path)) {
                    this.modalRef = this.subjectPopupService
                    .open(SubjectDeleteDialogComponent, params['login'] , true);
                }
            });


        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
