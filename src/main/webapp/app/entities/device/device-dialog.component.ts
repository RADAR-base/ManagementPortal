import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Device } from './device.model';
import { DevicePopupService } from './device-popup.service';
import { DeviceService } from './device.service';
import { DeviceType, DeviceTypeService } from '../device-type';
import { Project, ProjectService } from '../project';

@Component({
    selector: 'jhi-device-dialog',
    templateUrl: './device-dialog.component.html'
})
export class DeviceDialogComponent implements OnInit {

    device: Device;
    authorities: any[];
    isSaving: boolean;

    devicetypes: DeviceType[];

    projects: Project[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private deviceService: DeviceService,
        private deviceTypeService: DeviceTypeService,
        private projectService: ProjectService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['device']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_ADMIN'];
        this.deviceTypeService.query().subscribe(
            (res: Response) => { this.devicetypes = res.json(); }, (res: Response) => this.onError(res.json()));
        this.projectService.query().subscribe(
            (res: Response) => { this.projects = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.device.id !== undefined) {
            this.deviceService.update(this.device)
                .subscribe((res: Device) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.deviceService.create(this.device)
                .subscribe((res: Device) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Device) {
        this.eventManager.broadcast({ name: 'deviceListModification', content: 'OK'});
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

    trackProjectById(index: number, item: Project) {
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
    selector: 'jhi-device-popup',
    template: ''
})
export class DevicePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private devicePopupService: DevicePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.devicePopupService
                    .open(DeviceDialogComponent, params['id']);
            } else {
                this.modalRef = this.devicePopupService
                    .open(DeviceDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
