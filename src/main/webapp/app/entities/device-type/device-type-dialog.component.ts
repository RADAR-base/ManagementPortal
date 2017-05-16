import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { DeviceType } from './device-type.model';
import { DeviceTypePopupService } from './device-type-popup.service';
import { DeviceTypeService } from './device-type.service';

@Component({
    selector: 'jhi-device-type-dialog',
    templateUrl: './device-type-dialog.component.html'
})
export class DeviceTypeDialogComponent implements OnInit {

    deviceType: DeviceType;
    authorities: any[];
    isSaving: boolean;
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private deviceTypeService: DeviceTypeService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['deviceType']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_SYS_ADMIN'];
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.deviceType.id !== undefined) {
            this.deviceTypeService.update(this.deviceType)
                .subscribe((res: DeviceType) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.deviceTypeService.create(this.deviceType)
                .subscribe((res: DeviceType) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: DeviceType) {
        this.eventManager.broadcast({ name: 'deviceTypeListModification', content: 'OK'});
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
}

@Component({
    selector: 'jhi-device-type-popup',
    template: ''
})
export class DeviceTypePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private deviceTypePopupService: DeviceTypePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.deviceTypePopupService
                    .open(DeviceTypeDialogComponent, params['id']);
            } else {
                this.modalRef = this.deviceTypePopupService
                    .open(DeviceTypeDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
