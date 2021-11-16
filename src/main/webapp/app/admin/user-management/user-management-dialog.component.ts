import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageHelper, User, UserService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { Role } from './role.model';

import { UserModalService } from './user-modal.service';
import { Subscription } from "rxjs";

@Component({
    selector: 'jhi-user-mgmt-dialog',
    templateUrl: './user-management-dialog.component.html',
})
export class UserMgmtDialogComponent implements OnInit, OnDestroy {
    user: User;
    isAdmin: boolean;
    roles: Role[];
    isSaving: Boolean;
    private subscriptions: Subscription;

    constructor(
      public activeModal: NgbActiveModal,
      public languageHelper: JhiLanguageHelper,
      private userService: UserService,
      private eventManager: EventManager,
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.registerChangeInRoles();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    registerChangeInRoles() {
        this.subscriptions = this.eventManager.subscribe('roleListModification', (response) => {
            this.user.roles = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.user.id) {
            this.userService.update(this.user).subscribe(
              (response) => this.onSaveSuccess(response),
              () => this.onSaveError()
            );
        } else {
            this.userService.create(this.user).subscribe(
              (response) => this.onSaveSuccess(response),
              () => this.onSaveError()
            );
        }
    }

    private onSaveSuccess(result) {
        this.eventManager.broadcast({name: 'userListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError() {
        this.isSaving = false;
    }
}

@Component({
    selector: 'jhi-user-dialog',
    template: '',
})
export class UserDialogComponent implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private userModalService: UserModalService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.route.url.subscribe(url => {
                if ('user-management-new-admin' === (url[0].path)) {
                    this.modalRef = this.userModalService.open(UserMgmtDialogComponent, null, true);
                }
                return;
            });
            this.modalRef = this.userModalService.open(UserMgmtDialogComponent, params['login'], false);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
