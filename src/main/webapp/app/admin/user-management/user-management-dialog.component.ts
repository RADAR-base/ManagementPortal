import {Component, OnInit, OnDestroy, OnChanges, SimpleChanges} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { UserModalService } from './user-modal.service';
import { JhiLanguageHelper, User, UserService } from '../../shared';
import { Project, ProjectService} from '../../entities/project';
import {RoleService} from "../../entities/role/role.service";
import {Authority} from "../../shared/user/authority.model";
import {AuthorityService} from "../../shared/user/authority.service";
import {ADMIN_AUTHORITY} from "../../shared/constants/common.constants";
import {Role} from "./role.model";

@Component({
    selector: 'jhi-user-mgmt-dialog',
    templateUrl: './user-management-dialog.component.html'
})
export class UserMgmtDialogComponent implements OnInit {

    user: User;
    isAdmin: boolean;
    languages: any[];
    roles: Role[];
    defaultRoles: Role[];
    isSaving: Boolean;

    constructor(
        public activeModal: NgbActiveModal,
        private languageHelper: JhiLanguageHelper,
        private jhiLanguageService: JhiLanguageService,
        private userService: UserService,
        private roleService: RoleService,
        private authorityService: AuthorityService,
        private eventManager: EventManager
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
        this.jhiLanguageService.setLocations(['user-management']);
        this.registerChangeInRoles();
    }

    registerChangeInRoles() {
        this.eventManager.subscribe('roleListModification', (response ) => {
            this.user.roles = response.content;
        });
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.user.id !== null) {
            this.userService.update(this.user).subscribe((response) => this.onSaveSuccess(response), () => this.onSaveError());
        } else {
            this.userService.create(this.user).subscribe((response) => this.onSaveSuccess(response), () => this.onSaveError());
        }
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option == selectedVals[i]) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }

    private onSaveSuccess(result) {
        this.eventManager.broadcast({ name: 'userListModification', content: 'OK' });
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError() {
        this.isSaving = false;
    }
}

@Component({
    selector: 'jhi-user-dialog',
    template: ''
})
export class UserDialogComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private userModalService: UserModalService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.route.url.subscribe(url =>{
                if('user-management-new-admin'==(url[0].path)){
                    this.modalRef = this.userModalService.open(UserMgmtDialogComponent , null, true);
                }
                return;
            });
            if ( params['login'] ) {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent,params['login'], false );
            } else {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent, null , false);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
