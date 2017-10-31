import {
    Component, OnInit, OnDestroy} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Response} from '@angular/http';

import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {EventManager, AlertService, JhiLanguageService} from 'ng-jhipster';

import {Subject} from './subject.model';
import {SubjectPopupService} from './subject-popup.service';
import {SubjectService} from './subject.service';
import {MinimalSource} from "../source/source.model";

@Component({
    selector: 'jhi-subject-dialog',
    templateUrl: './subject-dialog.component.html'
})
export class SubjectDialogComponent implements OnInit {

    subject: Subject;
    authorities: any[];
    isSaving: boolean;

    // sources: MinimalSource[];
    options: string[];
    attributeComponentEventPrefix: 'subjectAttributes';

    constructor(public activeModal: NgbActiveModal,
                private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private subjectService: SubjectService,
                private eventManager: EventManager) {
        this.jhiLanguageService.setLocations(['subject', 'project', 'projectStatus']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_SYS_ADMIN'];
        this.options = ['Human-readable-identifier'];
        this.registerChangesInSubject();
    }

    private registerChangesInSubject() {
        this.eventManager.subscribe(this.attributeComponentEventPrefix + 'ListModification', (response) => {
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
export class SubjectPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(private route: ActivatedRoute,
                private subjectPopupService: SubjectPopupService,) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            let projectName: string;
            if (params['projectName']) {
                projectName = params['projectName'];
            }
            if (params['login']) {
                this.modalRef = this.subjectPopupService
                .open(SubjectDialogComponent, params['login'], false, projectName);
            } else {
                this.modalRef = this.subjectPopupService
                .open(SubjectDialogComponent, null, false, projectName);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
