import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { Project, ProjectService } from '../../shared/project';
import { SourceDataService } from '../source-data';
import { SourceTypePopupService } from './source-type-popup.service';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';
import { Subscription } from "rxjs";
import { switchMap } from "rxjs/operators";
import { SourceTypeDeleteDialogComponent } from "./source-type-delete-dialog.component";

@Component({
    selector: 'jhi-source-type-dialog',
    templateUrl: './source-type-dialog.component.html',
})
export class SourceTypeDialogComponent {
    readonly authorities: string[];

    sourceType: SourceType;
    isSaving: boolean;

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private sourceTypeService: SourceTypeService,
            private sourceDataService: SourceDataService,
            public projectService: ProjectService,
            private eventManager: EventManager,
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
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
    private modalRef: NgbModalRef;
    private routeSub: Subscription;

    constructor(
      private route: ActivatedRoute,
      private sourceTypePopupService: SourceTypePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.pipe(
          switchMap(params => this.sourceTypePopupService.open(
            SourceTypeDialogComponent, params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion']
          )),
        ).subscribe(modalRef => this.modalRef = modalRef);
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
        this.modalRef?.dismiss();
    }
}
