import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../../shared/util/event-manager.service';
import { SourceTypePopupService } from './source-type-popup.service';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';
import { Subscription } from "rxjs";
import { switchMap } from "rxjs/operators";

@Component({
    selector: 'jhi-source-type-delete-dialog',
    templateUrl: './source-type-delete-dialog.component.html',
})
export class SourceTypeDeleteDialogComponent {

    sourceType: SourceType;

    constructor(
            private sourceTypeService: SourceTypeService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(producer: string, model: string, version: string) {
        this.sourceTypeService.delete(producer, model, version).subscribe(() => {
            this.eventManager.broadcast({
                name: 'sourceTypeListModification',
                content: 'Deleted an sourceType',
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-type-delete-popup',
    template: '',
})
export class SourceTypeDeletePopupComponent implements OnInit, OnDestroy {
    routeSub: Subscription;
    private modalRef: NgbModalRef;

    constructor(
            private route: ActivatedRoute,
            private sourceTypePopupService: SourceTypePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.pipe(
          switchMap(params => this.sourceTypePopupService.open(
            SourceTypeDeleteDialogComponent, params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion']
          )),
        ).subscribe(modalRef => this.modalRef = modalRef);
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
        this.modalRef?.dismiss();
    }
}
