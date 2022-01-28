import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageHelper, User, UserService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { Role } from './role.model';

import { UserModalService } from './user-modal.service';

@Component({
    selector: 'jhi-user-mgmt-dialog',
    templateUrl: './user-management-dialog.component.html',
})
export class UserMgmtDialogComponent implements OnInit {
    user: User;
    isAdmin: boolean;
    languages: any[];
    roles: Role[];
    isSaving: Boolean;

    constructor(
            public activeModal: NgbActiveModal,
            private languageHelper: JhiLanguageHelper,
            private userService: UserService,
            private eventManager: EventManager,
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
        this.registerChangeInRoles();
    }

    registerChangeInRoles() {
        this.eventManager.subscribe('roleListModification', (response) => {
            this.user.roles = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.user.id !== null) {
            this.userService.update(this.user)
            .subscribe((response) => this.onSaveSuccess(response), () => this.onSaveError());
        } else {
            this.userService.create(this.user)
            .subscribe((response) => this.onSaveSuccess(response), () => this.onSaveError());
        }
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (!selectedVals) {
            return option;
        }
        const idx = selectedVals.indexOf(option);
        return idx === -1 ? option : selectedVals[idx];
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
