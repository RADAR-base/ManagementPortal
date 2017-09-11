import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Source } from './source.model';
import { SourcePopupService } from './source-popup.service';
import { SourceService } from './source.service';
import { DeviceType, DeviceTypeService } from '../device-type';
import {MinimalProject, Project} from "../project/project.model";
import {ProjectService} from "../project/project.service";

@Component({
    selector: 'jhi-source-dialog',
    templateUrl: './source-dialog.component.html'
})
export class SourceDialogComponent implements OnInit {

    source: Source;
    authorities: any[];
    isSaving: boolean;
    deviceTypes: DeviceType[];
    projects: MinimalProject[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private sourceService: SourceService,
        private projectService: ProjectService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['source']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        // this.sourceTypeService.query().subscribe(
        //     (res: Response) => { this.sourcetypes = res.json(); }, (res: Response) => this.onError(res.json()));
        this.projectService.findAll(true).subscribe(
            (res: Response) => { this.projects = res.json(); }, (res: Response) => this.onError(res.json()));
        if(this.source.project) {
            this.projectService.findDeviceTypesById(this.source.project.id).subscribe((res: Response) => {
                this.deviceTypes = res.json();
            });
        }
    }

    public onProjectChange(project: any) {
        if(project!=null) {
            this.projectService.findDeviceTypesById(project.id).subscribe((res: Response) => {
                this.deviceTypes = res.json();
            });
        }
        else {
            this.deviceTypes = null;
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
    trackProjectById(index: number, item: MinimalProject) {
        return item.id;
    }

    getSelected(selectedVals: any, option: any) {
        console.log('selected', selectedVals);
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
            if ( params['id'] ) {
                this.modalRef = this.sourcePopupService
                    .open(SourceDialogComponent, params['id']);
            } else {
                this.modalRef = this.sourcePopupService
                    .open(SourceDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
