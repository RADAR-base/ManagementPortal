import {combineLatest, merge, Observable, OperatorFunction, Subject, Subscription} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map} from 'rxjs/operators';
import {Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';

import {
    NgbActiveModal,
    NgbCalendar,
    NgbDate,
    NgbDateParserFormatter,
    NgbDateStruct,
    NgbModalRef,
    NgbTypeahead,
    NgbTypeaheadSelectItemEvent
} from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { SourceType, SourceTypeService } from '../source-type';
import { ProjectPopupService } from './project-popup.service';

import {GroupService, Organization, OrganizationService, Principal, Project, ProjectService} from '../../shared';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';

@Component({
    selector: 'jhi-project-dialog',
    templateUrl: './project-dialog.component.html',
    styleUrls: ['project-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class ProjectDialogComponent implements OnInit, OnDestroy {
    readonly options: string[];

    organizationName: string;
    organizations: Organization[];

    project: Project;
    projectCopy: Project;
    isSaving: boolean;
    projectIdAsPrettyValue: boolean;

    sourceTypeInputText: string;

    attributeComponentEventPrefix: 'projectAttributes';

    startDate: NgbDateStruct;
    endDate: NgbDateStruct;

    @ViewChild('instance', {static: true}) instance: NgbTypeahead;
    focus$ = new Subject<string>();
    click$ = new Subject<string>();

    availableOrganizations: string[];
    hasRoleToChangeOrganization: boolean = false;

    getMatchingSourceTypes: OperatorFunction<string, SourceType[]> = (text$: Observable<string>) => {
        const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
        const clicksWithClosedPopup$ = this.click$.pipe(filter(() => !this.instance.isPopupOpen()));
        const inputFocus$ = this.focus$;

        const availableTypes$ = this.sourceTypeService.sourceTypes$.pipe(
            map(sourceTypes => {
                const selectedTypeIds = new Set(this.projectCopy?.sourceTypes?.map(t => t.id) || []);
                return sourceTypes.filter(t => !selectedTypeIds.has(t.id));
            })
        );
        return combineLatest([
            merge(debouncedText$, inputFocus$, clicksWithClosedPopup$),
            availableTypes$
        ]).pipe(
            map(([term, availableTypes]) => {
                term = term.trim().toLowerCase();
                if (!term) {
                    return availableTypes;
                }
                const getTypeKey = t => this.formatSourceTypeOption(t).toLowerCase();
                return availableTypes.filter(t => getTypeKey(t).includes(term));
            })
        )
    };

    private subscriptions = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private organizationService: OrganizationService,
            private projectService: ProjectService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
            private groupService: GroupService,
            private calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter,
            private router: Router,
            public principal: Principal,
    ) {
        this.isSaving = false;
        this.options = ['Work-package', 'Phase', 'External-project-url', 'External-project-id', 'Privacy-policy-url'];
        this.projectIdAsPrettyValue = true;
    }

    ngOnInit() {
        const organizationSubscription = this.organizationService.organizations$.subscribe(organizations => this.organizations = organizations)
        this.subscriptions.add(organizationSubscription);

        this.projectCopy = Object.assign({}, this.project)
        if(this.projectCopy.startDate) {
            this.startDate = this.formatter.parse(this.projectCopy.startDate.toString());
        }
        if(this.projectCopy.endDate) {
            this.endDate = this.formatter.parse(this.projectCopy.endDate.toString());
        }
        this.subscriptions.add(this.registerChangesToAttributes());

        const principalSubscription = this.principal.account$.subscribe(account => {
            const isSysAdmin = account.roles.find(role => role.authorityName === 'ROLE_SYS_ADMIN')
            if(isSysAdmin){
                this.hasRoleToChangeOrganization = true;
                this.availableOrganizations = this.organizations.map(o => o.name)
            } else {
                this.hasRoleToChangeOrganization = !!account.roles.find(role => role.organizationName === this.organizationName)
                if(this.hasRoleToChangeOrganization){
                    this.availableOrganizations = account.roles
                        .filter(role => role.authorityName === "ROLE_ORGANIZATION_ADMIN")
                        .map(role => role.organizationName)
                } else {
                    this.availableOrganizations = this.organizations.map(o => o.name)
                }
            }
        })

        this.subscriptions.add(principalSubscription);
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
            this.projectCopy.startDate = this.formatter.format(this.startDate) + 'T00:00';
        }
        if (this.endDate && this.calendar.isValid(NgbDate.from(this.endDate))) {
            this.projectCopy.endDate = this.formatter.format(this.endDate) + 'T23:59';
        }
        if (this.projectCopy.id !== undefined) {
            const organization = this.organizations.find(o => o.name === this.projectCopy.organization.name)
            const updatedProject = {...this.projectCopy, organization};
            this.subscriptions.add(this.projectService.update(updatedProject).subscribe(
                (res: Project) => this.onSaveSuccess(res),
                (res: Response) => this.onSaveError(res),
            ));
        } else {
            const organization = this.organizations.find(o => o.name === this.organizationName)
            const updatedProject = {...this.projectCopy, organization};
            this.subscriptions.add(this.projectService.create(updatedProject).subscribe(
                (res: Project) => this.onSaveSuccess(res),
                (res: Response) => this.onSaveError(res),
            ));
        }
    }

    private registerChangesToAttributes(): Subscription {
        return this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.projectCopy.attributes = response.content;
        });
    }

    private onSaveSuccess(result: Project) {
        if(history.state?.parentComponent === 'project-detail') {
            this.router.navigate(['/project', result.projectName]);
        }
        this.eventManager.broadcast({name: 'projectListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        this.isSaving = false;
    }

    addSourceType(event: NgbTypeaheadSelectItemEvent) {
        const sourceType = event.item as SourceType;
        const currentSourceTypes = this.projectCopy.sourceTypes || [];
        this.projectCopy.sourceTypes = [ ...currentSourceTypes, sourceType ];
        this.sourceTypeInputText = '';
        event.preventDefault();
    }

    removeSourceType(id: number) {
        this.projectCopy.sourceTypes = this.projectCopy.sourceTypes.filter(t => t.id !== id);
    }

    formatSourceTypeOption(t: SourceType) {
        return `${t.producer}_${t.model}_${t.catalogVersion}`;
    }
}

@Component({
    selector: 'jhi-project-popup',
    template: '',
})
export class ProjectPopupComponent extends ObservablePopupComponent {
    constructor(
            route: ActivatedRoute,
            private projectPopupService: ProjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.projectPopupService.open(ProjectDialogComponent, params['organizationName'], params['projectName']);
    }
}
