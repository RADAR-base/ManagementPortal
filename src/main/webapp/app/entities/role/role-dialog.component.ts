import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import {RolePopupService} from "./role-popup.service";
import {RoleService} from "./role.service";
import {Project, ProjectService} from "../project";
import {Principal, AuthorityService} from "../../shared";
import {Role} from "../../admin/user-management/role.model";

@Component({
    selector: 'jhi-role-dialog',
    templateUrl: './role-dialog.component.html'
})
export class RoleDialogComponent implements OnInit {

    role: Role;
    authorities: any[];
    projects: Project[];
    isSaving: boolean;
    currentAccount: any;

    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private roleService: RoleService,
        private authorityService: AuthorityService,
        private projectService: ProjectService,
        private principal: Principal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.addLocation('role');
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorityService.findAll().subscribe( res => {
            this.authorities = res.json();
        });
        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            } );

    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    trackProjectByName(index: number, item: Project) {
        return item.projectName;
    }

    save() {
        this.isSaving = true;
        if (this.role.id !== undefined) {
            this.roleService.update(this.role)
                .subscribe((res: Role) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        } else {
            this.roleService.create(this.role)
                .subscribe((res: Role) =>
                    this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
        }
    }

    private onSaveSuccess(result: Role) {
        this.eventManager.broadcast({ name: 'roleListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }
}

@Component({
    selector: 'jhi-source-data-popup',
    template: ''
})
export class RolePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private rolePopupService: RolePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['projectName'] && params['authorityName'] ) {
                this.modalRef = this.rolePopupService
                    .open(RoleDialogComponent, params['projectName'], params['authorityName']);
            } else {
                this.modalRef = this.rolePopupService
                    .open(RoleDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
