import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {AlertService} from '../util/alert.service';
import {EventManager} from '../util/event-manager.service';
import {ProjectService} from '../project/project.service';
import {SourceType} from '../../entities/source-type';
import {SourcePopupService} from './source-popup.service';

import {Source} from './source.model';
import {SourceService} from './source.service';
import {ObservablePopupComponent} from '../util/observable-popup.component';
import {Observable} from 'rxjs';

@Component({
    selector: 'jhi-source-dialog',
    templateUrl: './source-dialog.component.html',
})
export class SourceDialogComponent implements OnInit {
    readonly authorities: string[];
    readonly options: string[];

    source: Source;
    isSaving: boolean;
    sourceTypes: SourceType[];
    attributeComponentEventPrefix: 'sourceAttributes';

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: AlertService,
        private sourceService: SourceService,
        private projectService: ProjectService,
        private eventManager: EventManager,
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['External-identifier'];
    }

    ngOnInit() {
        if (this.source.project) {
            this.projectService.findSourceTypesByName(this.source.project.projectName).subscribe((res: any) => {
                this.sourceTypes = res;
            });
        }
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.source.attributes = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.source.id !== undefined) {
            this.sourceService.update(this.source)
                .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: any) => this.onSaveError(res));
        } else {
            this.sourceService.create(this.source)
                .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: any) => this.onSaveError(res));
        }
    }

    trackSourceTypeById(index: number, item: SourceType) {
        return item.id;
    }

    private onSaveSuccess(result: Source) {
        this.eventManager.broadcast({name: 'sourceListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}

@Component({
    selector: 'jhi-source-popup',
    template: '',
})
export class SourcePopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private sourcePopupService: SourcePopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.sourcePopupService.open(SourceDialogComponent, params['sourceName'], params['projectName']);
    }
}
