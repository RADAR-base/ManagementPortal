import { Component, OnInit, OnDestroy } from '@angular/core';
import { ObservablePopupComponent } from '../../util/observable-popup.component';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subscription } from 'rxjs';
import { ActivatedRoute, Params } from '@angular/router';
import { AlertService } from '../../util/alert.service';
import { Subject } from '../subject.model';
import { QueryParticipantService } from '../query-participant.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { SubjectPopupService } from '../subject-popup.service';
import { QueryGroup, QueryParticipant } from 'app/shared/queries/query.model';

@Component({
    selector: 'app-query-viewer',
    templateUrl: './query-viewer.component.html',
    styleUrls: ['./query-viewer.component.scss'],
})
export class QueryViewerComponent implements OnInit, OnDestroy {
    subject: Subject;

    queryGroupList: QueryGroup[];

    selectedGroup: number;

    queryPriticipant: QueryParticipant = {};

    private subscriptions: Subscription = new Subscription();

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: AlertService,
        private queryParticipantService: QueryParticipantService
    ) {}

    ngOnInit() {
        if (this.subject.id !== null) {
            this.queryParticipantService
                .getAllQueryGroups()
                .subscribe((res) => {
                    this.queryGroupList = res;
                });
        }
    }

    changeSelectGroup() {
        console.log(this.selectedGroup);
    }

    assignQueryGroup() {
        if (this.selectedGroup) {
            this.queryPriticipant.queryGroupId = this.selectedGroup;
            this.queryPriticipant.subjectId = this.subject.id;

            this.queryParticipantService
                .assignQueryGroup(this.queryPriticipant)
                .subscribe((res) => {
                    console.log(res);
                });
        }
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
@Component({
    selector: 'jhi-subject-queries-popup',
    template: '',
})
export class SubjectQueriesViewerPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(
            QueryViewerComponent,
            params['login'],
            false
        );
    }
}
