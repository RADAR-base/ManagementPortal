import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from 'ng-jhipster';
import { EventManager } from '../../util/event-manager.service';
import { MinimalSource, SourceService} from '../../source';
import { SubjectPopupService } from '../subject-popup.service';

import { Subject } from '../subject.model';
import { SubjectService } from '../subject.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-source-assigner',
    templateUrl: './source-assigner.component.html',
})

export class SubjectSourceAssignerDialogComponent implements OnInit {
    subject: Subject;
    authorities: any[];
    assignableSources: MinimalSource[];
    assignedSources: MinimalSource[];
    isSaving: boolean;

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private subjectService: SubjectService,
            private sourceService: SourceService,
            private eventManager: EventManager,
    ) {
        this.isSaving = false;
    }

    ngOnInit() {
        if (this.subject.id !== null) {
            this.sourceService.findAvailable(this.subject.project.projectName).subscribe(
                    (res: HttpResponse<MinimalSource[]>) => {
                        this.assignableSources = res.body;
                    }, (res: HttpErrorResponse) => this.onError(res));
        }
        if (this.subject.id !== null) {
            this.assignedSources = this.subject.sources;
        }

    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.subject.id !== null) {
            this.subject.sources = this.assignedSources;
            this.subjectService.update(this.subject)
                    .subscribe((res: Subject) => this.onSaveSuccess(res),
                            (res: any) => this.onSaveError(res));
        } else {
            this.subjectService.create(this.subject)
                    .subscribe((res: Subject) => this.onSaveSuccess(res),
                            (res: any) => this.onSaveError(res));
        }
    }

    addSource(selectedSource: MinimalSource) {
        if (this.hasSource(selectedSource)) {
            this.alertService.error('managementPortalApp.source.sourceAlreadyAdded', null, null);
        } else {
            this.assignedSources.push(selectedSource);
            if (this.assignableSources.length > 0) {
                this.assignableSources = this.assignableSources.filter(obj => obj !== selectedSource);
            }
        }
    }

    trackDeviceById(index: number, item: MinimalSource) {
        return item.id;
    }

    hasSource(source: MinimalSource): boolean {
        return this.assignedSources.some(v => v.id === source.id);
    }

    removeSource(source: MinimalSource) {
        this.assignedSources = this.assignedSources.filter(obj => obj !== source);
        this.assignableSources.push(source);
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
}

@Component({
    selector: 'jhi-subject-source-pair-popup',
    template: '',
})
export class SubjectSourceAssignerPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if (params['login']) {
                this.modalRef = this.subjectPopupService
                        .open(SubjectSourceAssignerDialogComponent, params['login'], false);
            } else {
                console.log('Unknown subject found');
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
