import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { SourceType } from './source-type.model';
import { SourceTypePopupService } from './source-type-popup.service';
import { SourceTypeService } from './source-type.service';

@Component({
    selector: 'jhi-source-type-delete-dialog',
    templateUrl: './source-type-delete-dialog.component.html'
})
export class SourceTypeDeleteDialogComponent {

    sourceType: SourceType;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceTypeService: SourceTypeService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.addLocation('sourceType');
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(producer: string, model: string, version: string) {
        this.sourceTypeService.delete(producer, model, version).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'sourceTypeListModification',
                content: 'Deleted an sourceType'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-type-delete-popup',
    template: ''
})
export class SourceTypeDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourceTypePopupService: SourceTypePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourceTypePopupService
                .open(SourceTypeDeleteDialogComponent, params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
