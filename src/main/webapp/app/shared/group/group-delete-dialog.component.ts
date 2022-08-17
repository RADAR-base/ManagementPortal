import { Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { EventManager } from '../util/event-manager.service';
import { GroupService } from './group.service';
import { Group } from './group.model';
import { GroupPopupService } from './group-popup.service';
import { GroupDialogComponent } from './group-dialog.component';
import { ObservablePopupComponent } from '../util/observable-popup.component';
import { Observable } from 'rxjs';

@Component({
    selector: 'jhi-group-delete-dialog',
    templateUrl: './group-delete-dialog.component.html',
})
export class GroupDeleteDialogComponent {

    group: Group;

    constructor(
            private groupService: GroupService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete() {
        this.groupService.delete(this.group.projectName, this.group.name, true).subscribe(
                () => {
                    this.eventManager.broadcast({name: 'groupListModification', content: null});
                    this.activeModal.dismiss(true);
                }
            );
    }
}

@Component({
    selector: 'jhi-group-delete-popup',
    template: '',
})
export class GroupDeletePopupComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private groupPopupService: GroupPopupService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.groupPopupService.open(GroupDeleteDialogComponent, params['id'], true, params['projectName']);
    }
}
