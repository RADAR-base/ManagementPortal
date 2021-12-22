import { Observable, Subject, merge, combineLatest, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {
    NgbActiveModal,
    NgbCalendar, NgbDate, NgbDateParserFormatter,
    NgbDateStruct,
    NgbModalRef,
    NgbTypeaheadSelectItemEvent
} from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceType, SourceTypeService } from '../source-type';
import { ProjectPopupService } from './project-popup.service';

import {GroupService, OrganizationService, Project, ProjectService} from '../../shared';

@Component({
    selector: 'jhi-project-dialog',
    templateUrl: './project-dialog.component.html',
    styleUrls: ['project-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class ProjectDialogComponent implements OnInit, OnDestroy {
    readonly options: string[];

    organizationName: string;
    project: Project;
    isSaving: boolean;
    projectIdAsPrettyValue: boolean;

    sourceTypeInputText: string;
    sourceTypeInputFocus$ = new Subject<string>();

    newGroupInputText: string;

    attributeComponentEventPrefix: 'projectAttributes';

    startDate: NgbDateStruct;
    endDate: NgbDateStruct;

    private subscriptions = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            public organizationService: OrganizationService,
            private projectService: ProjectService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
            private groupService: GroupService,
            private calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter
    ) {
        this.isSaving = false;
        this.options = ['Work-package', 'Phase', 'External-project-url', 'External-project-id', 'Privacy-policy-url'];
        this.projectIdAsPrettyValue = true;
    }

    ngOnInit() {
        if(this.project.startDate) {
            this.startDate = this.formatter.parse(this.project.startDate.toString());
        }
        if(this.project.endDate) {
            this.endDate = this.formatter.parse(this.project.endDate.toString());
        }
        this.subscriptions.add(this.registerChangesToAttributes());
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.startDate && this.calendar.isValid(NgbDate.from(this.startDate))) {
            this.project.startDate = this.formatter.format(this.startDate) + 'T00:00';
        }
        if (this.endDate && this.calendar.isValid(NgbDate.from(this.endDate))) {
            this.project.endDate = this.formatter.format(this.endDate) + 'T23:59';
        }
        const updatedProject = {...this.project, organization: {name: this.organizationName}};
        if (this.project.id !== undefined) {
            this.subscriptions.add(this.projectService.update(updatedProject).subscribe(
              (res: Project) => this.onSaveSuccess(res),
              (res: Response) => this.onSaveError(res),
            ));
        } else {
            this.subscriptions.add(this.projectService.create(updatedProject).subscribe(
              (res: Project) => this.onSaveSuccess(res),
              (res: Response) => this.onSaveError(res),
            ));
        }
    }

    private registerChangesToAttributes(): Subscription {
        return this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.project.attributes = response.content;
        });
    }

    getMatchingSourceTypes(text$: Observable<string>): Observable<SourceType[]> {
        const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
        const inputFocus$ = this.sourceTypeInputFocus$;
        const availableTypes$ = this.sourceTypeService.sourceTypes$.pipe(
          map(sourceTypes => {
              const selectedTypeIds = new Set(this.project?.sourceTypes?.map(t => t.id) || []);
              return sourceTypes.filter(t => !selectedTypeIds.has(t.id));
          })
        );

        return combineLatest([
            merge(debouncedText$, inputFocus$),
            availableTypes$,
        ]).pipe(
          map(([term, availableTypes]) => {
              term = term.trim().toLowerCase();
              if (!term) {
                  return availableTypes;
              }
              const getTypeKey = t => this.formatSourceTypeOption(t).toLowerCase();
              return availableTypes.filter(t => getTypeKey(t).includes(term));
          }),
        );
    };

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

    addGroup() {
        let currentGroups = this.project.groups || [];
        let newGroup = { name: this.newGroupInputText };
        if (newGroup.name.length == 0 || newGroup.name.length > 50) {
            // TODO: actually show error
            return;
        }
        if (currentGroups.some(g => g.name === newGroup.name)) {
            // TODO: actually show error
            return;
        }
        this.groupService.create(this.project.projectName, newGroup).toPromise()
          .then(g => {
              this.project.groups = [ ...currentGroups, g];
              this.newGroupInputText = '';
          })
          .catch(reason => {
              // TODO: actually show error
          })
    }

    removeGroup(groupName: string) {
        // TODO: warn that this may affect existing subjects
        this.groupService.delete(this.project.projectName, groupName).toPromise()
          .then(() => {
              this.project.groups = this.project.groups.filter(g => g.name !== groupName);
          })
          .catch(() => {
              // TODO: actually show error
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
            console.log(params);
            this.modalRef = this.projectPopupService
                .open(ProjectDialogComponent, params['organizationName'], params['projectName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
