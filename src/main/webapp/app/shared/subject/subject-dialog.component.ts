import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {
    NgbActiveModal,
    NgbCalendar,
    NgbDate,
    NgbDateParserFormatter,
    NgbDateStruct,
    NgbModalRef
} from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { SubjectPopupService } from './subject-popup.service';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html',
})
export class SubjectDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];
    readonly options: string[];

    subject: Subject;
    isSaving: boolean;

    attributeComponentEventPrefix: 'subjectAttributes';

    dateOfBirth: NgbDateStruct;
    private eventSubscription: Subscription;

    constructor(public activeModal: NgbActiveModal,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private eventManager: EventManager,
                private calendar: NgbCalendar,
                private formatter: NgbDateParserFormatter) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['Human-readable-identifier'];
    }

    ngOnInit() {
        if(this.subject.dateOfBirth) {
            this.dateOfBirth = this.formatter.parse(this.subject.dateOfBirth.toString());
        }
        this.eventSubscription = this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.subject.attributes = response.content;
        });
    }

    ngOnDestroy() {
        this.eventSubscription.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.dateOfBirth && this.calendar.isValid(NgbDate.from(this.dateOfBirth))) {
            this.subject.dateOfBirth = new Date(this.formatter.format(this.dateOfBirth));
        }
        if (this.subject.id !== null) {
            this.subjectService.update(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess('UPDATE', res), (res: any) => this.onSaveError(res));
        } else {
            this.subjectService.create(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess('CREATE', res), (res: any) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(op: string, result: Subject) {
        this.eventManager.broadcast({name: 'subjectListModification', content: {op, subject: result}});
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
