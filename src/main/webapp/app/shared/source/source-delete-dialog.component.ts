import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { EventManager } from '../util/event-manager.service';
import { SourcePopupService } from './source-popup.service';

import { Source } from './source.model';
import { SourceService } from './source.service';

@Component({
    selector: 'jhi-source-delete-dialog',
    templateUrl: './source-delete-dialog.component.html',
})
export class SourceDeleteDialogComponent {

    source: Source;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private sourceService: SourceService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('source');
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(sourceName: string) {
        this.sourceService.delete(sourceName).subscribe(() => {
            this.eventManager.broadcast({
                name: 'sourceListModification',
                content: 'Deleted a source',
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-delete-popup',
    template: '',
})
export class SourceDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private sourcePopupService: SourcePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourcePopupService
                    .open(SourceDeleteDialogComponent, params['sourceName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
