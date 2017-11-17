import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { SourceData } from './source-data.model';
import { SourceDataPopupService } from './source-data-popup.service';
import { SourceDataService } from './source-data.service';

@Component({
    selector: 'jhi-source-data-delete-dialog',
    templateUrl: './source-data-delete-dialog.component.html'
})
export class SourceDataDeleteDialogComponent {

    sourceData: SourceData;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceDataService: SourceDataService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['sourceData', 'dataType']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(sensorName: string) {
        this.sourceDataService.delete(sensorName).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'sourceDataListModification',
                content: 'Deleted an sourceData'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-data-delete-popup',
    template: ''
})
export class SourceDataDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourceDataPopupService: SourceDataPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourceDataPopupService
                .open(SourceDataDeleteDialogComponent, params['sensorName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
