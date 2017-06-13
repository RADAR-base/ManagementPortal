import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Patient } from './patient.model';
import { PatientPopupService } from './patient-popup.service';
import { PatientService } from './patient.service';
// import { Usr, UsrService } from '../usr';
import { Device, DeviceService } from '../device';
import {Project} from "../project/project.model";
import {ProjectService} from "../project/project.service";
import {MinimalDevice} from "../device/device.model";

@Component({
    selector: 'jhi-patient-dialog',
    templateUrl: './patient-dialog.component.html'
})
export class PatientDialogComponent implements OnInit {

    patient: Patient;
    authorities: any[];
    isSaving: boolean;
    projects: Project[];
    login ?= 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';

    devices: MinimalDevice[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private patientService: PatientService,
        private projectService: ProjectService,
        private deviceService: DeviceService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['patient']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            } );
        this.patient.login = this.login.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });

        // this.usrService.query({filter: 'patient-is-null'}).subscribe((res: Response) => {
        //     if (!this.patient.userId) {
        //         this.users = res.json();
        //     } else {
        //         this.usrService.find(this.patient.userId).subscribe((subRes: Usr) => {
        //             this.users = [subRes].concat(res.json());
        //         }, (subRes: Response) => this.onError(subRes.json()));
        //     }
        // }, (res: Response) => this.onError(res.json()));
        this.deviceService.findUnAssigned().subscribe(
            (res: Response) => { this.devices = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.patient.id !== null) {
            this.patientService.update(this.patient)
                .subscribe((res: Patient) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.patientService.create(this.patient)
                .subscribe((res: Patient) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Patient) {
        this.eventManager.broadcast({ name: 'patientListModification', content: 'OK'});
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

    trackDeviceById(index: number, item: MinimalDevice) {
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
    selector: 'jhi-patient-popup',
    template: ''
})
export class PatientPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private patientPopupService: PatientPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.patientPopupService
                    .open(PatientDialogComponent, params['id']);
            } else {
                this.modalRef = this.patientPopupService
                    .open(PatientDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
