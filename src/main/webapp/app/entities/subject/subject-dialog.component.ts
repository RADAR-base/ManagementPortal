import {Component, OnInit, OnDestroy, Input} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Response} from '@angular/http';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';

import {Subject} from './subject.model';
import {SubjectPopupService} from './subject-popup.service';
import {SubjectService} from './subject.service';
import {SourceService} from '../source';
import {Project} from "../project/project.model";
import {ProjectService} from "../project/project.service";
import {MinimalSource} from "../source/source.model";

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html'
})
export class SubjectDialogComponent implements OnInit {

    subject: Subject;
    authorities: any[];
    isSaving: boolean;
    projects: Project[];

    sources: MinimalSource[];

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private projectService: ProjectService,
                private sourceService: SourceService,
                private eventManager: EventManager) {
        this.jhiLanguageService.setLocations(['subject']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            });
        if (this.subject.id !== null) {
            this.sourceService.findUnAssignedAndOfSubject(this.subject.id).subscribe(
                (res: Response) => {
                    this.sources = res.json();
                }, (res: Response) => this.onError(res.json()));
        } else {
            this.sourceService.findUnAssigned().subscribe(
                (res: Response) => {
                    this.sources = res.json();
                }, (res: Response) => this.onError(res.json()));
        }
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
                if (option.id === selectedVals[i].id) {
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
export class SubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if (params['id']) {
                this.modalRef = this.subjectPopupService
                .open(SubjectDialogComponent, params['id']);
            } else {
                this.modalRef = this.subjectPopupService
                .open(SubjectDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
