import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { SourceData } from './source-data.model';
import { SourceDataPopupService } from './source-data-popup.service';
import { SourceDataService } from './source-data.service';
import { DeviceType, DeviceTypeService } from '../device-type';

@Component({
    selector: 'jhi-source-data-dialog',
    templateUrl: './source-data-dialog.component.html'
})
export class SourceDataDialogComponent implements OnInit {

    sourceData: SourceData;
    authorities: any[];
    isSaving: boolean;

    devicetypes: DeviceType[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private sourceDataService: SourceDataService,
        private deviceTypeService: DeviceTypeService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['sourceData', 'dataType']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN' , 'ROLE_PROJECT_ADMIN'];
        this.deviceTypeService.query().subscribe(
            (res: Response) => { this.devicetypes = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sourceData.id !== undefined) {
            this.sourceDataService.update(this.sourceData)
                .subscribe((res: SourceData) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.sourceDataService.create(this.sourceData)
                .subscribe((res: SourceData) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: SourceData) {
        this.eventManager.broadcast({ name: 'sourceDataListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    trackDeviceTypeById(index: number, item: DeviceType) {
        return item.id;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
}

@Component({
    selector: 'jhi-source-data-popup',
    template: ''
})
export class SourceDataPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourceDataPopupService: SourceDataPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['sensorName'] ) {
                this.modalRef = this.sourceDataPopupService
                    .open(SourceDataDialogComponent, params['sensorName']);
            } else {
                this.modalRef = this.sourceDataPopupService
                    .open(SourceDataDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
