import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { SensorData } from './sensor-data.model';
import { SensorDataPopupService } from './sensor-data-popup.service';
import { SensorDataService } from './sensor-data.service';
import { DeviceType, DeviceTypeService } from '../device-type';

@Component({
    selector: 'jhi-sensor-data-dialog',
    templateUrl: './sensor-data-dialog.component.html'
})
export class SensorDataDialogComponent implements OnInit {

    sensorData: SensorData;
    authorities: any[];
    isSaving: boolean;

    devicetypes: DeviceType[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private sensorDataService: SensorDataService,
        private deviceTypeService: DeviceTypeService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['sensorData', 'dataType']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_SYS_ADMIN'];
        this.deviceTypeService.query().subscribe(
            (res: Response) => { this.devicetypes = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sensorData.id !== undefined) {
            this.sensorDataService.update(this.sensorData)
                .subscribe((res: SensorData) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.sensorDataService.create(this.sensorData)
                .subscribe((res: SensorData) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: SensorData) {
        this.eventManager.broadcast({ name: 'sensorDataListModification', content: 'OK'});
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
    selector: 'jhi-sensor-data-popup',
    template: ''
})
export class SensorDataPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sensorDataPopupService: SensorDataPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.sensorDataPopupService
                    .open(SensorDataDialogComponent, params['id']);
            } else {
                this.modalRef = this.sensorDataPopupService
                    .open(SensorDataDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
