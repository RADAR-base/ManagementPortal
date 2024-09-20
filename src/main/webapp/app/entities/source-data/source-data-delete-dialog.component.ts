import {Component} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {EventManager} from '../../shared/util/event-manager.service';
import {SourceDataPopupService} from './source-data-popup.service';

import {SourceData} from './source-data.model';
import {SourceDataService} from './source-data.service';
import {ObservablePopupComponent} from '../../shared/util/observable-popup.component';
import {Observable} from 'rxjs';

@Component({
    selector: 'jhi-source-data-delete-dialog',
    templateUrl: './source-data-delete-dialog.component.html',
})
export class SourceDataDeleteDialogComponent {
    sourceData: SourceData;

    constructor(
        private sourceDataService: SourceDataService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager,
    ) {
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
export class SourceDataDeletePopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private sourceDataPopupService: SourceDataPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.sourceDataPopupService.open(SourceDataDeleteDialogComponent, params['sourceDataName']);
    }
}
