import { Component, OnDestroy, OnInit } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, EventManager, JhiLanguageService } from 'ng-jhipster';
import { Source, SourceService } from '../../shared/source';
import { MinimalProject, ProjectService } from '../../shared';

import { SourceType } from '../source-type';
import { GeneralSourcePopupService } from './general-source-popup.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-source-dialog',
    templateUrl: './general-source-dialog.component.html',
})
export class GeneralSourceDialogComponent implements OnInit {
    readonly authorities: string[];

    source: Source;
    isSaving: boolean;
    sourceTypes: SourceType[];
    projects: MinimalProject[];

    constructor(
            public activeModal: NgbActiveModal,
            private jhiLanguageService: JhiLanguageService,
            private alertService: AlertService,
            private sourceService: SourceService,
            private projectService: ProjectService,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('source');
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.isSaving = false;
    }

    ngOnInit() {
        this.projectService.findAll(true).subscribe(
                (res: MinimalProject[]) => {
                    this.projects = res;
                }, (res: HttpErrorResponse) => this.onError(res));

        this.onProjectChange(this.source.project);
    }

    public onProjectChange(project: any) {
        if (project) {
            this.projectService.findSourceTypesByName(project.projectName)
                    .subscribe((res: SourceType[]) => this.sourceTypes = res);
        } else {
            this.sourceTypes = null;
        }
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.source.id !== undefined) {
            this.sourceService.update(this.source)
            .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.sourceService.create(this.source)
            .subscribe((res: Source) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Source) {
        this.eventManager.broadcast({name: 'sourceListModification', content: 'OK'});
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

    trackSourceTypeById(index: number, item: SourceType) {
        return item.id;
    }

    trackProjectById(index: number, item: MinimalProject) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-source-popup',
    template: '',
})
export class GeneralSourcePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private sourcePopupService: GeneralSourcePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.sourcePopupService
                    .open(GeneralSourceDialogComponent, params['sourceName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
