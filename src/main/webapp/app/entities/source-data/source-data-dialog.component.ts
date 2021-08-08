import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceType, SourceTypeService } from '../source-type';
import { SourceDataPopupService } from './source-data-popup.service';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';

@Component({
    selector: 'jhi-source-data-dialog',
    templateUrl: './source-data-dialog.component.html',
})
export class SourceDataDialogComponent implements OnInit {

    sourceData: SourceData;
    authorities: any[];
    isSaving: boolean;

    sourceTypes: SourceType[];

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private sourceDataService: SourceDataService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.sourceTypeService.query().subscribe(
                (res: HttpResponse<SourceType[]>) => {
                    this.sourceTypes = res.body;
                }, (res: HttpErrorResponse) => this.onError(res));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sourceData.id !== undefined) {
            this.sourceDataService.update(this.sourceData)
            .subscribe((res: SourceData) =>
                    this.onSaveSuccess(res), (res: HttpErrorResponse) => this.onSaveError(res));
        } else {
            this.sourceDataService.create(this.sourceData)
            .subscribe((res: SourceData) =>
                    this.onSaveSuccess(res), (res: HttpErrorResponse) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: SourceData) {
        this.eventManager.broadcast({name: 'sourceDataListModification', content: 'OK'});
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

    trackSourceTypeById(index: number, item: SourceType) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-source-data-popup',
    template: '',
})
export class SourceDataPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private sourceDataPopupService: SourceDataPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourceDataPopupService
                    .open(SourceDataDialogComponent, params['sourceDataName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
