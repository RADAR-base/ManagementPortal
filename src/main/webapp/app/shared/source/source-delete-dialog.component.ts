import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { Source } from './source.model';
import { SourcePopupService } from './source-popup.service';
import { SourceService } from './source.service';

@Component({
    selector: 'jhi-source-delete-dialog',
    templateUrl: './source-delete-dialog.component.html'
})
export class SourceDeleteDialogComponent {

    source: Source;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceService: SourceService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['source']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.sourceService.delete(id).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'sourceListModification',
                content: 'Deleted an source'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-source-delete-popup',
    template: ''
})
export class SourceDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourcePopupService: SourcePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourcePopupService
                .open(SourceDeleteDialogComponent, params['id']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
