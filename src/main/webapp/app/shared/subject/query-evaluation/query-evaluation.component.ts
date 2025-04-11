import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { HttpClient } from '@angular/common/http';

import { AlertService } from '../../util/alert.service';

import { SubjectPopupService } from '../subject-popup.service';

import { Subject } from '../subject.model';
import { SubjectService } from '../subject.service';
import { ObservablePopupComponent } from '../../util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-query-evaluation',
    templateUrl: './query-evaluation.component.html',
})

export class QueryEvaluationDialogComponent implements OnInit, OnDestroy {
    subject: Subject;


    public dataexample: string;

    private baseUrl = 'api/public';
    dataLogs:any = {};

    private subscriptions: Subscription = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private subjectService: SubjectService,
             private http: HttpClient
    ) {
    }

    ngOnInit() {
        if (this.subject.id !== null) {

            this.subjectService.findDataLogs(this.subject.login).subscribe((response: HttpResponse<any>) => {
                  this.dataLogs = response.body.reduce(function(acc, cur, i) {
                                    acc[cur.groupingType] = new Date(cur.time).toDateString();
                                    return acc;
                                  }, {});
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

    evaluate() {
        var json = JSON.parse(this.dataexample);

        this.http
                .post(this.baseUrl + '/query/evaluate', json)
                .subscribe((id) => {
                   console.log("response", id)
                   });
    }
}

@Component({
    selector: 'jhi-subject-data-viewer-popup',
    template: '',
})
export class QueryEvaluationPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(QueryEvaluationDialogComponent, params['login'], false);
    }
}
