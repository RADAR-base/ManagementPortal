import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, JhiLanguageService } from 'ng-jhipster';
import { EventManager } from '../../shared/util/event-manager.service';
import { Project, ProjectService } from '../../shared/project';
import { SourceDataService } from '../source-data';
import { SourceTypePopupService } from './source-type-popup.service';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-source-type-dialog',
    templateUrl: './source-type-dialog.component.html',
})
export class SourceTypeDialogComponent implements OnInit {
    readonly authorities: string[];

    sourceType: SourceType;
    isSaving: boolean;

    projects: Project[];

    constructor(
            public activeModal: NgbActiveModal,
            private jhiLanguageService: JhiLanguageService,
            private alertService: AlertService,
            private sourceTypeService: SourceTypeService,
            private sourceDataService: SourceDataService,
            private projectService: ProjectService,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('sourceType');
        this.jhiLanguageService.addLocation('sourceTypeScope');
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
    }

    ngOnInit() {
        this.projectService.query().subscribe(
                (res: HttpResponse<any>) => this.projects = res.body,
                (res: HttpErrorResponse) => this.onError(res));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sourceType.id !== undefined) {
            this.sourceTypeService.update(this.sourceType)
                    .subscribe((res: SourceType) => this.onSaveSuccess(res),
                            (res: HttpErrorResponse) => this.onSaveError(res));
        } else {
            this.sourceTypeService.create(this.sourceType)
                    .subscribe((res: SourceType) => this.onSaveSuccess(res),
                            (res: HttpErrorResponse) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: SourceType) {
        this.eventManager.broadcast({name: 'sourceTypeListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error: HttpErrorResponse) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}

@Component({
    selector: 'jhi-source-type-popup',
    template: '',
})
export class SourceTypePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private sourceTypePopupService: SourceTypePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourceTypePopupService
                    .open(SourceTypeDialogComponent, params['sourceTypeProducer'],
                            params['sourceTypeModel'], params['catalogVersion']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
