import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Project } from './project.model';
import { ProjectPopupService } from './project-popup.service';
import { ProjectService } from './project.service';
import { DeviceType, DeviceTypeService } from '../device-type';

@Component({
    selector: 'jhi-project-dialog',
    templateUrl: './project-dialog.component.html'
})
export class ProjectDialogComponent implements OnInit  {

    project: Project;
    authorities: any[];
    isSaving: boolean;

    devicetypes: DeviceType[];
    keys : string[];
    attributeComponentEventPrefix : 'projectAttributes';
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private projectService: ProjectService,
        private deviceTypeService: DeviceTypeService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['project', 'projectStatus' , 'source' , 'subject' , 'user-management']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN' , 'ROLE_PROJECT_ADMIN'];
        this.deviceTypeService.query().subscribe(
            (res: Response) => { this.devicetypes = res.json(); }, (res: Response) => this.onError(res.json()));
        this.keys = ['Work-package', 'Phase' , 'External-project-url' , 'External-project-id'];
        this.registerChangesInProject();
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.project.id !== undefined) {
            this.projectService.update(this.project)
                .subscribe((res: Project) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.projectService.create(this.project)
                .subscribe((res: Project) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Project) {
        this.eventManager.broadcast({ name: 'projectListModification', content: 'OK'});
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

    private registerChangesInProject() {
        this.eventManager.subscribe(this.attributeComponentEventPrefix+'ListModification', (response ) => {
            this.project.attributes= response.content;
        });
    }
}

@Component({
    selector: 'jhi-project-popup',
    template: ''
})
export class ProjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private projectPopupService: ProjectPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['projectName'] ) {
                this.modalRef = this.projectPopupService
                    .open(ProjectDialogComponent, params['projectName']);
            } else {
                this.modalRef = this.projectPopupService
                    .open(ProjectDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
