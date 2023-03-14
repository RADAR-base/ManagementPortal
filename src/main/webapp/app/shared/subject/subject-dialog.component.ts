import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';

import {
    NgbActiveModal,
    NgbCalendar,
    NgbDate,
    NgbDateParserFormatter,
    NgbDateStruct,
    NgbModalRef
} from '@ng-bootstrap/ng-bootstrap';

import {AlertService} from '../util/alert.service';
import {EventManager} from '../util/event-manager.service';
import {SubjectPopupService} from './subject-popup.service';

import {Subject} from './subject.model';
import {SubjectService} from './subject.service';
import {Observable, Subscription} from 'rxjs';
import {ObservablePopupComponent} from '../util/observable-popup.component';
import {Project, ProjectService} from "../project";
import {Group, GroupService} from "../group";

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html',
})
export class SubjectDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];
    readonly options: string[];

    subject: Subject;
    isInProject: boolean;
    projects: Project[] = [];
    groups$: Observable<Group[]>;
    project: Project;

    groupName: string;

    isSaving: boolean;

    attributeComponentEventPrefix: 'subjectAttributes';

    dateOfBirth: NgbDateStruct;
    private subscriptions: Subscription = new Subscription();

    constructor(public activeModal: NgbActiveModal,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private projectService: ProjectService,
                private groupService: GroupService,
                private eventManager: EventManager,
                private calendar: NgbCalendar,
                private formatter: NgbDateParserFormatter) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['Human-readable-identifier', 'participant_group'];
    }

    ngOnInit() {
        this.project = this.subject?.project;
        this.groups$ = this.groupService.list(this.project.projectName)
        this.groupName = this.subject.group || null;

        this.projectService.query().subscribe((projects) => {
            this.projects = projects.body;
        })
        if(this.subject.dateOfBirth) {
            this.dateOfBirth = this.formatter.parse(this.subject.dateOfBirth.toString());
        }
        this.subscriptions.add(this.registerEventChanges());
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    private registerEventChanges(): Subscription {
        return this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
            this.subject.attributes = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.dateOfBirth && this.calendar.isValid(NgbDate.from(this.dateOfBirth))) {
            this.subject.dateOfBirth = new Date(this.formatter.format(this.dateOfBirth));
        }
        this.subject.project = this.project;

        this.subject.group = this.groupName; //this.project.groups.find(group => group.name === this.groupName)

        if (this.subject.id) {
            this.subjectService.update(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess('UPDATE', res), (res: any) => this.onSaveError(res));
        } else {
            this.subjectService.create(this.subject)
            .subscribe((res: Subject) =>
                    this.onSaveSuccess('CREATE', res), (res: any) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(op: string, result: Subject) {
        this.eventManager.broadcast({name: 'subjectListModification', content: {op, subject: result}});
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

    onProjectChange($event: any) {
        this.project = this.projects.find((p) => p.projectName === $event);
    }
}

@Component({
    selector: 'jhi-subject-popup',
    template: '',
})
export class SubjectPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(SubjectDialogComponent, params['login'], false, params['projectName']);
    }
}
