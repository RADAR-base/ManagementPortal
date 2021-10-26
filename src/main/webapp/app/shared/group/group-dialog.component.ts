import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { GroupPopupService } from './group-popup.service';
import {Group} from "./group.model";
import {GroupService} from "./group.service";

@Component({
    selector: 'jhi-group-dialog',
    templateUrl: './group-dialog.component.html',
})
export class GroupDialogComponent {

    group: Group;
    isSaving: boolean;

    constructor(public activeModal: NgbActiveModal,
                private alertService: AlertService,
                private groupService: GroupService,
                private eventManager: EventManager) {
        this.isSaving = false;
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        this.groupService.create(this.group.projectName, this.group)
            .subscribe(
                    (res: Group) => this.onSaveSuccess(res),
                    (res: any) => this.onSaveError(res)
            );
    }

    private onSaveSuccess(result: Group) {
        this.eventManager.broadcast({name: 'groupListModification', content: result});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}

@Component({
    selector: 'jhi-group-popup',
    template: '',
})
export class GroupPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private groupPopupService: GroupPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            console.log(params)
            this.modalRef = this.groupPopupService
                    .open(GroupDialogComponent, params['id'], false, params['projectName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
