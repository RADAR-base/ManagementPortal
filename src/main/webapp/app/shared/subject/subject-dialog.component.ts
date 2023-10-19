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

import { Delusion } from '../model/delusion.model';
import { delusions } from 'content/jsons/delusions';


@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html',
})
export class SubjectDialogComponent implements OnInit, OnDestroy {
    readonly authorities: string[];
    readonly options: string[];
    delusions: Delusion[] = delusions;

    subject: Subject;
    isInProject: boolean;
    projects: Project[] = [];
    groups$ : Observable<Group[]>;
    project: Project;

    groupName: string;

    delusion1 : Delusion;
    delusion2 : Delusion;

    isSaving: boolean;
    creationError: boolean = false;
    IDNameError: boolean = false;

    attributeComponentEventPrefix: 'subjectAttributes';

    dateOfBirth: NgbDateStruct;
    private subscriptions: Subscription = new Subscription();

    subjects: String[]=[];
    IDUniqueError:boolean = false;


    constructor(public activeModal: NgbActiveModal,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private projectService: ProjectService,
                private groupService: GroupService,
                private eventManager: EventManager,
                private calendar: NgbCalendar,
                private formatter: NgbDateParserFormatter,
                public route:ActivatedRoute) {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];

    }

    ngOnInit() {
        this.project = this.subject?.project;
        this.groups$  = this.groupService.list(this.project.projectName)
        this.groupName = this.subject.group || null;

        this.projectService.query().subscribe((projects) => {
            this.projects = projects.body;
        })
        if(this.subject.dateOfBirth) {
            this.dateOfBirth = this.formatter.parse(this.subject.dateOfBirth.toString());
        }

       this.delusion1  = this.subject.attributes?.delusion_1 ? {...this.delusions.find((d=>d.key==this.subject.attributes.delusion_1))}: {...this.delusions[12]};
       this.delusion2  = this.subject.attributes?.delusion_2 ? {...this.delusions.find((d=>d.key==this.subject.attributes.delusion_2))} : {...this.delusions[12]};

      this.subscriptions.add(this.registerEventChanges());


      this.subjectService.findAllExternalIds().subscribe(v => {
            this.subjects = v.body
      })
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
        this.creationError =false;
        this.IDNameError = false;
        this.IDUniqueError =false;
        var subjectID = this.subject.externalId.trim();
        if(subjectID != null&&subjectID!="") {
            this.creationError = false;

            if(this.IDPatternCheck(subjectID)){
                this.IDNameError = false;
                if(this.IDUniqueCheck(subjectID)){
                    this.IDUniqueError = false;
                    this.isSaving = true;
                    if (this.dateOfBirth && this.calendar.isValid(NgbDate.from(this.dateOfBirth))) {
                        this.subject.dateOfBirth = new Date(this.formatter.format(this.dateOfBirth));
                    }
                    this.subject.attributes = {}
                    if(this.delusion1  != null) {
                        this.subject.attributes.delusion_1 =this.delusion1.key;
                    }

                    if(this.delusion2  != null) {
                        this.subject.attributes.delusion_2 = this.delusion2.key;
                    }

                    this.subject.project = this.project;

                    this.subject.group = this.groupName; //this.project.groups$.find(group => group.name === this.groupName)


                    if (this.subject.id) {
                        this.subjectService.update(this.subject)
                        .subscribe((res: Subject) =>
                                this.onSaveSuccess('UPDATE', res), (res: any) => this.onSaveError(res));
                    } else {
                        this.subjectService.create(this.subject)
                        .subscribe((res: Subject) =>
                                this.onSaveSuccess('CREATE', res), (res: any) => this.onSaveError(res));
                    }

                }else{
                    this.IDUniqueError = true;
                }

        }else {
            this.IDNameError = true;
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

    onProjectChange( $event: any) {
        this.project = this.projects.find((p) => p.projectName ===  $event);
    }

    onDelusion1Change( $event: any){
        this.delusion1  = {...this.delusions.find((d => d.key ===  $event))};
    }

    onDelusion2Change( $event: any){
        this.delusion2  = {...this.delusions.find((d => d.key ===  $event))};
    }

    getDelusionsChoice1() {
       return this.delusions.filter(o => o.key != this.delusion2.key ||o.key=="none");
    }

    getDelusionsChoice2() {
       return this.delusions.filter(o => o.key != this.delusion1.key ||o.key=="none");
    }

    IDPatternCheck(ID:string){
        var reg = /^WS2_(M|C|E|G|K|S)[A-Z]{2}[0-9]{3}$/;
        return (reg.test(ID))
    }

    IDUniqueCheck (ID:string){
        return !this.subjects.some(function(el){ return el === ID})
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
