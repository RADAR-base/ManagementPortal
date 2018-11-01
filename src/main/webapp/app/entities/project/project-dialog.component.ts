import { Component, OnDestroy, OnInit } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, EventManager, JhiLanguageService } from 'ng-jhipster';
import { SourceType, SourceTypeService } from '../source-type';
import { ProjectPopupService } from './project-popup.service';

import { Project, ProjectService } from '../../shared/project';

@Component({
    selector: 'jhi-project-dialog',
    templateUrl: './project-dialog.component.html',
})
export class ProjectDialogComponent implements OnInit {

    project: Project;
    authorities: any[];
    isSaving: boolean;
    projectIdAsPrettyValue: boolean;

    sourceTypes: SourceType[];
    options: string[];
    attributeComponentEventPrefix: 'projectAttributes';

    constructor(
            public activeModal: NgbActiveModal,
            private jhiLanguageService: JhiLanguageService,
            private alertService: AlertService,
            private projectService: ProjectService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.setLocations(['project', 'projectStatus']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.sourceTypeService.query().subscribe(
                (res: Response) => {
                    this.sourceTypes = res.json();
                }, (res: Response) => this.onError(res.json()));
        this.options = ['Work-package', 'Phase', 'External-project-url', 'External-project-id', 'Privacy-policy-url'];
        this.registerChangesInProject();
        this.projectIdAsPrettyValue = true;
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.project.id !== undefined) {
            this.projectService.update(this.project)
            .subscribe((res: Project) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.projectService.create(this.project)
            .subscribe((res: Project) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Project) {
        this.eventManager.broadcast({name: 'projectListModification', content: 'OK'});
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

    private registerChangesInProject() {
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.project.attributes = response.content;
        });
    }
}

@Component({
    selector: 'jhi-project-popup',
    template: '',
})
export class ProjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private projectPopupService: ProjectPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if (params['projectName']) {
                this.modalRef = this.projectPopupService
                .open(ProjectDialogComponent, params['projectName']);
            } else {
                this.modalRef = this.projectPopupService
                .open(ProjectDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
