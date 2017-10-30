import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { SensorData } from './sensor-data.model';
import { SensorDataService } from './sensor-data.service';
@Injectable()
export class SensorDataPopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private sensorDataService: SensorDataService

    ) {}

    open(component: Component, sensorName?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (sensorName) {
            this.sensorDataService.find(sensorName).subscribe((sensorData) => {
                this.sensorDataModalRef(component, sensorData);
            });
        } else {
            return this.sensorDataModalRef(component, new SensorData());
        }
    }

    sensorDataModalRef(component: Component, sensorData: SensorData): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.sensorData = sensorData;
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
