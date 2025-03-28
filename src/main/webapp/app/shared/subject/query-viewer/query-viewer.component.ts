import { Component, OnInit, OnDestroy } from '@angular/core';
import { ObservablePopupComponent } from '../../util/observable-popup.component';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subscription } from 'rxjs';
import { ActivatedRoute, Params } from '@angular/router';
import { AlertService } from '../../util/alert.service';
import { Subject } from '../subject.model';
import { SubjectService } from '../subject.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { SubjectPopupService } from '../subject-popup.service';


@Component({
  selector: 'app-query-viewer',
  templateUrl: './query-viewer.component.html',
  styleUrls: ['./query-viewer.component.scss']
})
export class QueryViewerComponent implements OnInit, OnDestroy {
    subject: Subject;

    dataLogs:any = {};

    private subscriptions: Subscription = new Subscription();

    constructor(
            public activeModal: NgbActiveModal,
            private alertService: AlertService,
            private subjectService: SubjectService,
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
}
@Component({
  selector: 'jhi-subject-queries-popup',
  template: '',
})
export class SubjectQueriesViewerPopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private subjectPopupService: SubjectPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.subjectPopupService.open(QueryViewerComponent, params['login'], false);
    }
}