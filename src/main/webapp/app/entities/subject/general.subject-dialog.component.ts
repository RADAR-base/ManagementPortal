import {
    Component, OnInit, OnDestroy
} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Response} from '@angular/http';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';

import {Project} from "../project/project.model";
import {ProjectService} from "../project/project.service";
import {Subject} from "../../shared/subject/subject.model";
import {SubjectService} from "../../shared/subject/subject.service";
import {GeneralSubjectPopupService} from "./general.subject-popup.service";
import {MinimalSource} from "../../shared/source/source.model";
import {SourceService} from "../../shared/source/source.service";
@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './general.subject-dialog.component.html'
})
export class GeneralSubjectDialogComponent implements OnInit {

    subject: Subject;
    authorities: any[];
    isSaving: boolean;
    projects: Project[];

    // sources: MinimalSource[];
    keys : string[];
    attributeComponentEventPrefix : 'subjectAttributes';

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private projectService: ProjectService,
                private sourceService: SourceService,
                private eventManager: EventManager) {
        this.jhiLanguageService.setLocations(['subject' , 'project' , 'projectStatus']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.keys = ['Human-readable-identifier'];
        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            });
        this.registerChangesInSubject();
    }

    private registerChangesInSubject() {
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification',
            (response) => {
                this.subject.attributes = response.content;
            });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.subject.id !== null) {
            this.subjectService.update(this.subject)
            .subscribe((res: Subject) =>
                this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.subjectService.create(this.subject)
            .subscribe((res: Subject) =>
                this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Subject) {
        this.eventManager.broadcast({name: 'subjectListModification', content: 'OK'});
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

    trackDeviceById(index: number, item: MinimalSource) {
        return item.id;
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (selectedVals[i] && option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
}

@Component({
    selector: 'jhi-subject-popup',
    template: ''
})
export class GeneralSubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: GeneralSubjectPopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if (params['id']) {
                this.modalRef = this.subjectPopupService
                .open(GeneralSubjectDialogComponent, params['id']);
            } else {
                this.modalRef = this.subjectPopupService
                .open(GeneralSubjectDialogComponent);
            }
        });

    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
