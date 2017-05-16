import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { SensorData } from './sensor-data.model';
import { SensorDataPopupService } from './sensor-data-popup.service';
import { SensorDataService } from './sensor-data.service';

@Component({
    selector: 'jhi-sensor-data-delete-dialog',
    templateUrl: './sensor-data-delete-dialog.component.html'
})
export class SensorDataDeleteDialogComponent {

    sensorData: SensorData;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sensorDataService: SensorDataService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['sensorData', 'dataType']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.sensorDataService.delete(id).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'sensorDataListModification',
                content: 'Deleted an sensorData'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-sensor-data-delete-popup',
    template: ''
})
export class SensorDataDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sensorDataPopupService: SensorDataPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sensorDataPopupService
                .open(SensorDataDeleteDialogComponent, params['id']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
