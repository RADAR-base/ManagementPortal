import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { MinimalSource } from '../../shared/source';
import { Subject, SubjectService } from '../../shared/subject';

import { Project, ProjectService } from '../../shared';
import { GeneralSubjectPopupService } from './general.subject-popup.service';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './general.subject-dialog.component.html',
})
export class GeneralSubjectDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];
    readonly options: string[];

    subject: Subject;
    isSaving: boolean;
    attributeComponentEventPrefix: 'subjectAttributes';
    private eventSubscription: Subscription;

    constructor(public activeModal: NgbActiveModal,
                private alertService: AlertService,
                private subjectService: SubjectService,
                public projectService: ProjectService,
                private eventManager: EventManager) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['Human-readable-identifier'];
    }

    ngOnInit() {
        this.eventSubscription = this.eventManager.subscribe(
          this.attributeComponentEventPrefix + 'ListModification',
                (response) => this.subject.attributes = response.content);
    }

    ngOnDestroy() {
        this.eventSubscription.unsubscribe();
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

    trackProjectById(index: number, item: Project) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-subject-popup',
    template: '',
})
export class GeneralSubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: GeneralSubjectPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.subjectPopupService
                    .open(GeneralSubjectDialogComponent, params['login']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
