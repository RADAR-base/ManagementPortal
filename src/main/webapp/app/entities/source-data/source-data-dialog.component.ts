import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceType, SourceTypeService } from '../source-type';
import { SourceDataPopupService } from './source-data-popup.service';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-source-data-dialog',
    templateUrl: './source-data-dialog.component.html',
})
export class SourceDataDialogComponent implements OnInit, OnDestroy {
    sourceData: SourceData;
    isSaving: boolean;
    private subscriptions: Subscription = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private sourceDataService: SourceDataService,
            public sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sourceData.id !== undefined) {
            this.subscriptions.add(this.sourceDataService.update(this.sourceData).subscribe(
                (res: SourceData) => this.onSaveSuccess(res),
                (res: HttpErrorResponse) => this.onSaveError(res),
            ));
        } else {
            this.subscriptions.add(this.sourceDataService.create(this.sourceData).subscribe(
                (res: SourceData) => this.onSaveSuccess(res),
                (res: HttpErrorResponse) => this.onSaveError(res),
            ));
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
export class SourceDataPopupComponent extends ObservablePopupComponent {
    constructor(
            route: ActivatedRoute,
            private sourceDataPopupService: SourceDataPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.sourceDataPopupService.open(SourceDataDialogComponent, params['sourceDataName']);
    }
}
