import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { Source, SourceService } from '../../shared/source';
import { MinimalProject, Project, ProjectService } from '../../shared';

import { SourceType } from '../source-type';
import { GeneralSourcePopupService } from './general-source-popup.service';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-source-dialog',
    templateUrl: './general-source-dialog.component.html',
})
export class GeneralSourceDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];

    source: Source;
    isSaving: boolean;
    sourceTypes: SourceType[];

    projects: Project[];

    private subscriptions: Subscription = new Subscription();

    constructor(
      public activeModal: NgbActiveModal,
      private alertService: AlertService,
      private sourceService: SourceService,
      public projectService: ProjectService,
      private eventManager: EventManager,
    ) {
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.isSaving = false;
    }

    ngOnInit() {
        this.onProjectChange(this.source.project);
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    public onProjectChange(project: any) {
        if (project) {
            this.subscriptions.add(
                this.projectService.findSourceTypesByName(project.projectName).subscribe(
                    (res: SourceType[]) => this.sourceTypes = res
                )
            );
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
            this.subscriptions.add(this.sourceService.update(this.source).subscribe(
                (res: Source) => this.onSaveSuccess(res),
                (res: HttpErrorResponse) => this.onSaveError(res),
            ));
        } else {
            this.subscriptions.add(this.sourceService.create(this.source).subscribe(
                (res: Source) => this.onSaveSuccess(res),
                (res: HttpErrorResponse) => this.onSaveError(res),
            ));
        }
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
export class GeneralSourcePopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private sourcePopupService: GeneralSourcePopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.sourcePopupService.open(GeneralSourceDialogComponent, params['sourceName']);
    }
}
