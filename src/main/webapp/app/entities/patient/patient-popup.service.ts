import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Patient } from './patient.model';
import { PatientService } from './patient.service';
@Injectable()
export class PatientPopupService {
    private isOpen = false;
    login ?= 'xxxyxxxx';
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private patientService: PatientService

    ) {}

    open(component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.patientService.find(id).subscribe((patient) => {
                this.patientModalRef(component, patient);
            });
        } else {
            var patient = new Patient();
            patient.login = this.login.replace(/[xy]/g, function(c) {
                var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
                return v.toString(16);
            });
            return this.patientModalRef(component, patient);
        }
    }

    patientModalRef(component: Component, patient: Patient): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.patient = patient;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        });
        return modalRef;
    }
}
