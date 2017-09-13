import {
    Component, OnInit, OnDestroy
} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Response} from '@angular/http';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';
import {Subject} from "../../subject/subject.model";
import {MinimalSource} from "../../source/source.model";
import {SubjectService} from "../../subject/subject.service";
import {ProjectService} from "../project.service";
import {SourceService} from "../../source/source.service";
import {ProjectSubjectPopupService} from "./subject-popup.service";


@Component({
    selector: 'project-subject-dialog',
    templateUrl: './subject-dialog.component.html'
})
export class ProjectSubjectDialogComponent implements OnInit {

    subject: Subject;
    authorities: any[];
    isSaving: boolean;

    sources: MinimalSource[];

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
export class ProjectSubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private router : Router,
                private subjectPopupService: ProjectSubjectPopupService,
    ) {
    }

    ngOnInit() {
        this.router.routerState.root.firstChild.url.subscribe(url => {
            if(url[0].path === 'project' && url[1].path) {
                this.routeSub = this.route.params.subscribe((params) => {
                    if (params['id']) {
                        this.modalRef = this.subjectPopupService
                        .open(ProjectSubjectDialogComponent, params['id'] , url[1].path);
                    } else {
                        this.modalRef = this.subjectPopupService
                        .open(ProjectSubjectDialogComponent , null ,  url[1].path);
                    }
                });
            }
            else {
                this.routeSub = this.route.params.subscribe((params) => {
                    if (params['id']) {
                        this.modalRef = this.subjectPopupService
                        .open(ProjectSubjectDialogComponent, params['id']);
                    } else {
                        this.modalRef = this.subjectPopupService
                        .open(ProjectSubjectDialogComponent);
                    }
                });
            }
        });

    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
