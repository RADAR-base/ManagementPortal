import { Observable, Subject, merge } from 'rxjs';
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
import { OrganizationPopupService } from './organization-popup.service';

import { GroupService, Organization, OrganizationService } from '../../shared';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-organization-dialog',
    templateUrl: './organization-dialog.component.html',
    styleUrls: ['organization-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class OrganizationDialogComponent implements OnInit {
    readonly authorities: any[];
    readonly options: string[];

    organization: Organization;
    isSaving: boolean;
    organizationIdAsPrettyValue: boolean;

    // sourceTypes: SourceType[];

    // sourceTypeInputText: string;
    // sourceTypeInputFocus$ = new Subject<string>();
    // get sourceTypeOptions() {
    //     const selectedTypes = this.organization.sourceTypes || [];
    //     const selectedTypeIds = selectedTypes.map(t => t.id);
    //     return this.sourceTypes.filter(t => !selectedTypeIds.includes(t.id));
    // }

    // newGroupInputText: string;

    attributeComponentEventPrefix: 'organizationAttributes';

    // startDate: NgbDateStruct;
    // endDate: NgbDateStruct;

    // getMatchingSourceTypes = (text$: Observable<string>) => {
    //     const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    //     const inputFocus$ = this.sourceTypeInputFocus$;
    //
    //     return merge(debouncedText$, inputFocus$).pipe(map(term => {
    //         const availableTypes = this.sourceTypeOptions;
    //
    //         term = term.trim().toLowerCase();
    //         if (!term) {
    //             return availableTypes;
    //         }
    //         const getTypeKey = t => this.formatSourceTypeOption(t).toLowerCase();
    //         return availableTypes.filter(t => getTypeKey(t).includes(term));
    //     }));
    // }

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private organizationService: OrganizationService,
            private sourceTypeService: SourceTypeService,
            private eventManager: EventManager,
            private groupService: GroupService,
            private calendar: NgbCalendar,
            public formatter: NgbDateParserFormatter
    ) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN', 'ROLE_PROJECT_ADMIN'];
        this.options = ['Work-package', 'Phase', 'External-organization-url', 'External-organization-id', 'Privacy-policy-url'];
        this.organizationIdAsPrettyValue = true;
    }

    ngOnInit() {
        // if(this.organization.startDate) {
        //     this.startDate = this.formatter.parse(this.organization.startDate.toString());
        // }
        // if(this.organization.endDate) {
        //     this.endDate = this.formatter.parse(this.organization.endDate.toString());
        // }
        // this.sourceTypeService.query().subscribe(
        //         (res: HttpResponse<SourceType[]>) => {
        //             this.sourceTypes = res.body;
        //         }, (res: HttpErrorResponse) => this.onError(res));
        // this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
        //     this.organization.attributes = response.content;
        // });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        // if (this.startDate && this.calendar.isValid(NgbDate.from(this.startDate))) {
        //     this.organization.startDate = this.formatter.format(this.startDate) + 'T00:00';
        // }
        // if (this.endDate && this.calendar.isValid(NgbDate.from(this.endDate))) {
        //     this.organization.endDate = this.formatter.format(this.endDate) + 'T23:59';
        // }
        if (this.organization.id !== undefined) {
            this.organizationService.update(this.organization)
            .subscribe((res: Organization) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.organizationService.create(this.organization)
            .subscribe((res: Organization) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Organization) {
        this.eventManager.broadcast({name: 'organizationListModification', content: 'OK'});
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

    // addSourceType(event: NgbTypeaheadSelectItemEvent) {
    //     const sourceType = event.item as SourceType;
    //     const currentSourceTypes = this.organization.sourceTypes || [];
    //     this.organization.sourceTypes = [ ...currentSourceTypes, sourceType ];
    //     this.sourceTypeInputText = '';
    //     event.preventDefault();
    // }
    //
    // removeSourceType(id: number) {
    //     this.organization.sourceTypes = this.organization.sourceTypes.filter(t => t.id !== id);
    // }
    //
    // formatSourceTypeOption(t: SourceType) {
    //     return `${t.producer}_${t.model}_${t.catalogVersion}`;
    // }

    // addGroup() {
    //     let currentGroups = this.organization.groups || [];
    //     let newGroup = { name: this.newGroupInputText };
    //     if (newGroup.name.length == 0 || newGroup.name.length > 50) {
    //         // TODO: actually show error
    //         return;
    //     }
    //     if (currentGroups.some(g => g.name === newGroup.name)) {
    //         // TODO: actually show error
    //         return;
    //     }
    //     this.groupService.create(this.organization.organizationName, newGroup).toPromise()
    //       .then(g => {
    //           this.organization.groups = [ ...currentGroups, g];
    //           this.newGroupInputText = '';
    //       })
    //       .catch(reason => {
    //           // TODO: actually show error
    //       })
    // }
    //
    // removeGroup(groupName: string) {
    //     // TODO: warn that this may affect existing subjects
    //     this.groupService.delete(this.organization.organizationName, groupName).toPromise()
    //       .then(() => {
    //           this.organization.groups = this.organization.groups.filter(g => g.name !== groupName);
    //       })
    //       .catch(() => {
    //           // TODO: actually show error
    //       });
    // }
}

@Component({
    selector: 'jhi-organization-popup',
    template: '',
})
export class OrganizationPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private organizationPopupService: OrganizationPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.organizationPopupService
                    .open(OrganizationDialogComponent, params['organizationName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
