import { Observable, Subject, merge } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef, NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from 'ng-jhipster';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceType, SourceTypeService } from '../source-type';
import { ProjectPopupService } from './project-popup.service';

import { Project, ProjectService } from '../../shared/project';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-project-dialog',
    templateUrl: './project-dialog.component.html',
    styleUrls: ['project-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class ProjectDialogComponent implements OnInit {
    readonly authorities: any[];
    readonly options: string[];

    project: Project;
    isSaving: boolean;
    projectIdAsPrettyValue: boolean;

    sourceTypes: SourceType[];

    sourceTypeInputText: string;
    sourceTypeInputFocus$ = new Subject<string>();
    get sourceTypeOptions() {
        const selectedTypes = this.project.sourceTypes || [];
        const selectedTypeIds = selectedTypes.map(t => t.id);
        return this.sourceTypes.filter(t => !selectedTypeIds.includes(t.id));
    }

    attributeComponentEventPrefix: 'projectAttributes';

    getMatchingSourceTypes = (text$: Observable<string>) => {
        const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
        const inputFocus$ = this.sourceTypeInputFocus$;

        return merge(debouncedText$, inputFocus$).pipe(map(term => {
            const availableTypes = this.sourceTypeOptions;

            term = term.trim().toLowerCase();
            if (!term) {
                return availableTypes;
            }
            const getTypeKey = t => this.formatSourceTypeOption(t).toLowerCase();
            return availableTypes.filter(t => getTypeKey(t).includes(term));
        }));
    }

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private projectService: ProjectService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.options = ['Work-package', 'Phase', 'External-project-url', 'External-project-id', 'Privacy-policy-url'];
        this.projectIdAsPrettyValue = true;
    }

    ngOnInit() {
        this.sourceTypeService.query().subscribe(
                (res: HttpResponse<SourceType[]>) => {
                    this.sourceTypes = res.body;
                }, (res: HttpErrorResponse) => this.onError(res));
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.project.attributes = response.content;
        });
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

    addSourceType(event: NgbTypeaheadSelectItemEvent) {
        const sourceType = event.item as SourceType;
        const currentSourceTypes = this.project.sourceTypes || [];
        this.project.sourceTypes = [ ...currentSourceTypes, sourceType ];
        this.sourceTypeInputText = '';
        event.preventDefault();
    }

    removeSourceType(id: number) {
        this.project.sourceTypes = this.project.sourceTypes.filter(t => t.id !== id);
    }

    formatSourceTypeOption(t: SourceType) {
        return `${t.producer}_${t.model}_${t.catalogVersion}`;
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
            this.modalRef = this.projectPopupService
                    .open(ProjectDialogComponent, params['projectName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
