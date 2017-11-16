import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { DeviceType } from './device-type.model';
import { DeviceTypePopupService } from './device-type-popup.service';
import { DeviceTypeService } from './device-type.service';
import { SourceData, SourceDataService } from '../source-data';
import { Project, ProjectService } from '../project';

@Component({
    selector: 'jhi-device-type-dialog',
    templateUrl: './device-type-dialog.component.html'
})
export class DeviceTypeDialogComponent implements OnInit {

    deviceType: DeviceType;
    authorities: any[];
    isSaving: boolean;

    sourceData: SourceData[];

    projects: Project[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private deviceTypeService: DeviceTypeService,
        private sourceDataService: SourceDataService,
        private projectService: ProjectService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['deviceType', 'sourceType']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.sourceDataService.query().subscribe(
            (res: Response) => { this.sourceData = res.json(); }, (res: Response) => this.onError(res.json()));
        this.projectService.query().subscribe(
            (res: Response) => { this.projects = res.json(); }, (res: Response) => this.onError(res.json()));
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

    trackSourceDataById(index: number, item: SourceData) {
        return item.id;
    }

    trackProjectById(index: number, item: Project) {
        return item.projectName;
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
            if ( params['deviceTypeProducer'] &&  params['deviceTypeModel'] && params['catalogVersion']) {
                this.modalRef = this.deviceTypePopupService
                    .open(DeviceTypeDialogComponent, params['deviceTypeProducer'], params['deviceTypeModel'], params['catalogVersion']);
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
