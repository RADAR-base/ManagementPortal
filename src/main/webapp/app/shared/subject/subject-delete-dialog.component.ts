import { Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../util/event-manager.service';
import { SubjectPopupService } from './subject-popup.service';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { ObservablePopupComponent } from '../util/observable-popup.component';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
    selector: 'jhi-subject-delete-dialog',
    templateUrl: './subject-delete-dialog.component.html',
})
export class SubjectDeleteDialogComponent {
    subject: Subject;
    isDelete: boolean;

    constructor(
            private subjectService: SubjectService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(login: string) {
        if (this.isDelete) {
            this.subjectService.delete(login).subscribe(() => {
                this.activeModal.dismiss(true);
                window.history.back();
            });
        } else {
            this.subjectService.discontinue(this.subject).subscribe(() => {
                this.eventManager.broadcast({name: 'subjectListModification', content: {op: 'DELETE', subject: this.subject}});
                this.activeModal.dismiss(true);
                window.history.back();
            });
        }
    }
}

@Component({
    selector: 'jhi-subject-delete-popup',
    template: '',
})
export class SubjectDeletePopupComponent extends ObservablePopupComponent {
    constructor(
        private route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.route.url.pipe(
          switchMap(url => {
            if ('discontinue' === url[2].path) {
                return this.subjectPopupService.open(SubjectDeleteDialogComponent, params['login'], false);
            } else {
                return this.subjectPopupService.open(SubjectDeleteDialogComponent, params['login'], true);
            }
          }),
        );
    }
}
