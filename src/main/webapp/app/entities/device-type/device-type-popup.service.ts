import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { DeviceType } from './device-type.model';
import { DeviceTypeService } from './device-type.service';
@Injectable()
export class DeviceTypePopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private deviceTypeService: DeviceTypeService

    ) {}

    open(component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.deviceTypeService.find(id).subscribe((deviceType) => {
                this.deviceTypeModalRef(component, deviceType);
            });
        } else {
            return this.deviceTypeModalRef(component, new DeviceType());
        }
    }

    deviceTypeModalRef(component: Component, deviceType: DeviceType): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.deviceType = deviceType;
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
