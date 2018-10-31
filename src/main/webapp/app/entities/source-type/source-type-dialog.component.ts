import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { SourceType } from './source-type.model';
import { SourceTypePopupService } from './source-type-popup.service';
import { SourceTypeService } from './source-type.service';
import { SourceData, SourceDataService } from '../source-data';
import { Project, ProjectService } from '../project';

@Component({
    selector: 'jhi-source-type-dialog',
    templateUrl: './source-type-dialog.component.html'
})
export class SourceTypeDialogComponent implements OnInit {

    sourceType: SourceType;
    authorities: any[];
    isSaving: boolean;


    projects: Project[];
    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private sourceTypeService: SourceTypeService,
        private sourceDataService: SourceDataService,
        private projectService: ProjectService,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.addLocation('sourceType');
        this.jhiLanguageService.addLocation('sourceTypeScope');
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.projectService.query().subscribe(
            (res: Response) => { this.projects = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.sourceType.id !== undefined) {
            this.sourceTypeService.update(this.sourceType)
                .subscribe((res: SourceType) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.sourceTypeService.create(this.sourceType)
                .subscribe((res: SourceType) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: SourceType) {
        this.eventManager.broadcast({ name: 'sourceTypeListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    trackProjectById(index: number, item: Project) {
        return item.projectName;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
}

@Component({
    selector: 'jhi-source-type-popup',
    template: ''
})
export class SourceTypePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private sourceTypePopupService: SourceTypePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['sourceTypeProducer'] &&  params['sourceTypeModel'] && params['catalogVersion']) {
                this.modalRef = this.sourceTypePopupService
                    .open(SourceTypeDialogComponent, params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion']);
            } else {
                this.modalRef = this.sourceTypePopupService
                    .open(SourceTypeDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
