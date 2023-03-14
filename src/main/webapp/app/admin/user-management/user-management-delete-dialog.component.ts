import { Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { User, UserService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { UserModalService } from './user-modal.service';
import { ObservablePopupComponent } from '../../shared/util/observable-popup.component';
import { Observable } from 'rxjs';

@Component({
    selector: 'jhi-user-mgmt-delete-dialog',
    templateUrl: './user-management-delete-dialog.component.html',
})
export class UserMgmtDeleteDialogComponent {

    user: User;

    constructor(
            private userService: UserService,
            public activeModal: NgbActiveModal,
            private eventManager: EventManager,
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(login) {
        this.userService.delete(login).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'userListModification',
                content: 'Deleted a user',
            });
            this.activeModal.dismiss(true);
        });
    }

}

@Component({
    selector: 'jhi-user-delete-dialog',
    template: '',
})
export class UserDeleteDialogComponent extends ObservablePopupComponent {

    constructor(
            route: ActivatedRoute,
            private userModalService: UserModalService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.userModalService.open(UserMgmtDeleteDialogComponent, params['login']);
    }
}
