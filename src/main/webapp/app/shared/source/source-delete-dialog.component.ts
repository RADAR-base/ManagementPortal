import { Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../util/event-manager.service';
import { SourcePopupService } from './source-popup.service';

import { Source } from './source.model';
import { SourceService } from './source.service';
import { ObservablePopupComponent } from '../util/observable-popup.component';
import { Observable } from 'rxjs';

@Component({
    selector: 'jhi-source-delete-dialog',
    templateUrl: './source-delete-dialog.component.html',
})
export class SourceDeleteDialogComponent {

    source: Source;

    constructor(
            private sourceService: SourceService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
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
export class SourceDeletePopupComponent extends ObservablePopupComponent {
    constructor(
            route: ActivatedRoute,
            private sourcePopupService: SourcePopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.sourcePopupService.open(SourceDeleteDialogComponent, params['sourceName']);
    }
}
