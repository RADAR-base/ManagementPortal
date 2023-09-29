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
    delusions: any = [
             {key:"delusion_1", label:"I have felt like I could read other people's thoughts"},
             {key:"delusion_2", label:"I have felt like other people were reading my thoughts"},
             {key:"delusion_3", label:"I have felt that my thoughts were being controlled or influenced"},
             {key:"delusion_4", label:"I have felt like my thoughts were alien to me in some way"},
             {key:"delusion_5", label:"I have felt like the world is not real"},
             {key:"delusion_6", label:"I have felt like I am not real"},
             {key:"delusion_7", label:"I have felt like people were not what they seemed"},
             {key:"delusion_8", label:"I have felt like things on the TV, in books or magazines had a special meaning for me"},
             {key:"delusion_9", label:"I have felt like there was a conspiracy against me"},
             {key:"delusion_10", label:"I have been jealous"},
             {key:"delusion_11", label:"I have felt like something bad was about to happen"},
             {key:"delusion_11", label:"I have felt distinctly concerned about my physical health"},
             {key:"none", label:"none"}
 ];

    subject: Subject;
    isInProject: boolean;
    projects: Project[] = [];
    groups$: Observable<Group[]>;
    project: Project;

    groupName: string;

    delusion1$: string;
    delusion2$: string;

    isSaving: boolean;
    creationError: boolean = false;

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

       this.delusion1$ = this.subject.attributes?.delusion_1 ?? null;
       this.delusion2$ = this.subject.attributes?.delusion_2 ?? null;

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

        if(this.subject.externalId != null) {
                this.creationError = false;
                this.isSaving = true;
                if (this.dateOfBirth && this.calendar.isValid(NgbDate.from(this.dateOfBirth))) {
                    this.subject.dateOfBirth = new Date(this.formatter.format(this.dateOfBirth));
                }
                this.subject.attributes = {}
                if(this.delusion1$ != null) {
                     this.subject.attributes.delusion_1 = this.delusion1$;
                }

                if(this.delusion2$ != null) {
                    this.subject.attributes.delusion_2 = this.delusion2$;
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
        } else {
            this.creationError = true;
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

    getDelusionsChoice1() {
       return this.delusions.filter(o => o.key != this.delusion2$);
    }

    getDelusionsChoice2() {
       return this.delusions.filter(o => o.key != this.delusion1$);
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
