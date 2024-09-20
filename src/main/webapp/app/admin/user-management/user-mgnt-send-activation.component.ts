import {Component} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {NgbActiveModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {User, UserService} from '../../shared';
import {EventManager} from '../../shared/util/event-manager.service';
import {UserModalService} from './user-modal.service';
import {ObservablePopupComponent} from '../../shared/util/observable-popup.component';
import {Observable} from 'rxjs';

@Component({
    selector: 'jhi-user-mgnt-send-activation-dialog',
    templateUrl: './user-mgnt-send-activation.component.html',
})
export class UserSendActivationLinkDialogComponent {

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

    sendActivationLink(login) {
        this.userService.sendActivation(login).subscribe(() => {
            this.eventManager.broadcast({
                name: 'userListModification',
                content: 'Sent activation link a user',
            });
            this.activeModal.dismiss(true);
        });
    }

}

@Component({
    selector: 'jhi-user-send-activation-dialog',
    template: '',
})
export class UserSendActivationLinkComponent extends ObservablePopupComponent {
    constructor(
        route: ActivatedRoute,
        private userModalService: UserModalService,
    ) {
        super(route);
    }

    createModalRef(params: Params): Observable<NgbModalRef> {
        return this.userModalService.open(UserSendActivationLinkDialogComponent, params['login']);
    }
}
