import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { DeviceType } from './device-type.model';
import { DeviceTypePopupService } from './device-type-popup.service';
import { DeviceTypeService } from './device-type.service';

@Component({
    selector: 'jhi-device-type-delete-dialog',
    templateUrl: './device-type-delete-dialog.component.html'
})
export class DeviceTypeDeleteDialogComponent {

    deviceType: DeviceType;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private deviceTypeService: DeviceTypeService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['deviceType']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.deviceTypeService.delete(id).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'deviceTypeListModification',
                content: 'Deleted an deviceType'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-device-type-delete-popup',
    template: ''
})
export class DeviceTypeDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private deviceTypePopupService: DeviceTypePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.deviceTypePopupService
                .open(DeviceTypeDeleteDialogComponent, params['id']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
