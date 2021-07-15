import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, JhiLanguageService } from 'ng-jhipster';
import { EventManager } from '../util/event-manager.service';
import { MinimalSource } from '../source';
import { SubjectPopupService } from './subject-popup.service';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html',
})
export class SubjectDialogComponent implements OnInit {

    readonly authorities: string[];
    readonly options: string[];

    subject: Subject;
    isSaving: boolean;

    attributeComponentEventPrefix: 'subjectAttributes';

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private eventManager: EventManager) {
        this.jhiLanguageService.addLocation('subject');
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['Human-readable-identifier'];
    }

    ngOnInit() {
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.subject.attributes = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.subject.id !== null) {
            this.subjectService.update(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess(res), (res: any) => this.onSaveError(res));
        } else {
            this.subjectService.create(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess(res), (res: any) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Subject) {
        this.eventManager.broadcast({name: 'subjectListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    trackDeviceById(index: number, item: MinimalSource) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-subject-popup',
    template: '',
})
export class SubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.subjectPopupService
                    .open(SubjectDialogComponent, params['login'], false, params['projectName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
