import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ObservablePopupComponent } from '../../util/observable-popup.component';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subscription } from 'rxjs';
import { ActivatedRoute, Params } from '@angular/router';
import { AlertService } from '../../util/alert.service';
import { Subject } from '../subject.model';
import { QueryParticipantService } from '../query-participant.service';
import { SubjectPopupService } from '../subject-popup.service';
import { QueryGroup, QueryParticipant } from 'app/shared/queries/query.model';

@Component({
    selector: 'app-query-viewer',
    templateUrl: './query-viewer.component.html',
    styleUrls: ['./query-viewer.component.scss'],
})
export class QueryViewerComponent implements OnInit, OnDestroy {
    subject: Subject;

    @Input() queryGroupList: QueryGroup[];

    @Input() selectedGroup: number | null = null;

    queryPriticipant: QueryParticipant = {};

    @Input() assignedQueryGroups: QueryGroup[];

    ifDisable: boolean = true;

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
                    this.getAllAssignedGroups();
                });
        }
    }

    getAllAssignedGroups() {
        //also need to remove already assigned group

        this.queryParticipantService
            .getAllAssignedQueries(this.subject.id)
            .subscribe((res: QueryGroup[]) => {
                this.assignedQueryGroups = res;
                this.queryGroupList = this.queryGroupList.filter(
                    (o1) =>
                        !this.assignedQueryGroups.some(
                            (o2) => o1.name === o2.name
                        )
                );
            });
    }

    async assignQueryGroup() {
        if (this.selectedGroup) {
            this.queryPriticipant.queryGroupId = this.selectedGroup;
            this.queryPriticipant.subjectId = this.subject.id;

            this.queryParticipantService
                .assignQueryGroup(this.queryPriticipant)
                .subscribe((res) => {
                    this.getAllAssignedGroups();
                    this.removeQueryGroupFromList(this.selectedGroup);
                });
        }
    }

    removeQueryGroupFromList(queryGroupId) {
        this.queryGroupList = this.queryGroupList.filter((item) => {
            this.ifDisable = true;
            return item.id != queryGroupId;
        });
    }

    deleteAssignedGroup(queryGroup: QueryGroup) {
        this.queryParticipantService
            .deleteAssignedQueryGroup(queryGroup.id, this.subject.id)
            .subscribe(() => {
                this.getAllAssignedGroups();
                this.queryGroupList.push(queryGroup);
                this.selectedGroup = null;
            });
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    onGroupChange($event: any) {
        if ($event) this.ifDisable = false;
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
