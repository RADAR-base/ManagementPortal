import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../../util/alert.service';

import { SubjectPopupService } from '../subject-popup.service';

import { Subject } from '../subject.model';
import { SubjectService } from '../subject.service';
import { ObservablePopupComponent } from '../../util/observable-popup.component';
import { Observable, Subscription } from 'rxjs';

@Component({
    selector: 'jhi-data-viewer',
    templateUrl: './data-viewer.component.html',
})
// @ts-nocheck
export class SubjectDataViewerDialogComponent implements OnInit, OnDestroy {
    subject: Subject;

    dataLogs: any = {};

    private subscriptions: Subscription = new Subscription();

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: AlertService,
        private subjectService: SubjectService
    ) {}

    ngOnInit() {
        if (this.subject.id !== null) {
            this.subjectService
                .findDataLogs(this.subject.login)
                .subscribe((response: HttpResponse<any>) => {
                    this.dataLogs = response.body.reduce(function (
                        acc,
                        cur,
                        i
                    ) {
                        acc[cur.groupingType] = new Date(
                            cur.time
                        ).toDateString();
                        return acc;
                    },
                    {});
                });

            this.subjectService.findDataSummary(this.subject.login).subscribe(
                (response: HttpResponse<unknown>) => {
                    console.log('data summary', response);

                    // @ts-ignore
                    var binaryString = window.atob(response.body.fileBytes);
                    var binaryLen = binaryString.length;
                    var bytes = new Uint8Array(binaryLen);
                    for (var i = 0; i < binaryLen; i++) {
                        var ascii = binaryString.charCodeAt(i);
                        bytes[i] = ascii;
                    }
                    var blob = new Blob([bytes]);

                    var link = document.createElement('a');
                    link.href = window.URL.createObjectURL(blob);
                    link.download = 'filename.pdf';
                    link.click();
                },
                (response) => {
                    console.log('console log error', response);
                }
            );
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
    selector: 'jhi-subject-data-viewer-popup',
    template: '',
})
export class SubjectDataViewerPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(
            SubjectDataViewerDialogComponent,
            params['login'],
            false
        );
    }
}
