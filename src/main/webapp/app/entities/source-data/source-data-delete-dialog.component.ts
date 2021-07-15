import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceDataPopupService } from './source-data-popup.service';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';

@Component({
    selector: 'jhi-source-data-delete-dialog',
    templateUrl: './source-data-delete-dialog.component.html',
})
export class SourceDataDeleteDialogComponent {

    sourceData: SourceData;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private sourceDataService: SourceDataService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('sourceData');
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(sourceDataName: string) {
        this.sourceDataService.delete(sourceDataName).subscribe(() => {
            this.eventManager.broadcast({
                name: 'sourceDataListModification',
                content: 'Deleted an sourceData',
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-data-delete-popup',
    template: '',
})
export class SourceDataDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private sourceDataPopupService: SourceDataPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourceDataPopupService
                    .open(SourceDataDeleteDialogComponent, params['sourceDataName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
