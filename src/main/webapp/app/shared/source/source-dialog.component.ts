import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Source } from './source.model';
import { SourcePopupService } from './source-popup.service';
import { SourceService } from './source.service';
import {DeviceType} from "../../entities/device-type/device-type.model";
import {ProjectService} from "../../entities/project/project.service";

@Component({
    selector: 'jhi-source-dialog',
    templateUrl: './source-dialog.component.html'
})
export class SourceDialogComponent implements OnInit {

    source: Source;
    authorities: any[];
    isSaving: boolean;
    deviceTypes: DeviceType[];

    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private sourceService: SourceService,
        private projectService: ProjectService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['source' , 'project' , 'projectStatus']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        if(this.source.project) {
            this.projectService.findDeviceTypesByName(this.source.project.projectName).subscribe((res: Response) => {
                this.deviceTypes = res.json();
            });
        }
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.source.id !== undefined) {
            this.sourceService.update(this.source)
                .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.sourceService.create(this.source)
                .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Source) {
        this.eventManager.broadcast({ name: 'sourceListModification', content: 'OK'});
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

    getSelected(selectedVals: any, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (selectedVals[i] && option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
}

@Component({
    selector: 'jhi-source-popup',
    template: ''
})
export class SourcePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourcePopupService: SourcePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            let projectName: string;
            if (params['projectName']) {
                projectName = params['projectName'];
            }
            if ( params['sourceName'] ) {
                this.modalRef = this.sourcePopupService
                    .open(SourceDialogComponent, params['sourceName'] , projectName);
            } else {
                this.modalRef = this.sourcePopupService
                    .open(SourceDialogComponent , null,  projectName);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
