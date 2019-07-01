import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AlertService, EventManager, JhiLanguageService } from 'ng-jhipster';
import { Role } from '../../admin/user-management/role.model';
import { AuthorityService, Principal, Project, ProjectService } from '../../shared';

import { RolePopupService } from './role-popup.service';
import { RoleService } from './role.service';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-role-dialog',
    templateUrl: './role-dialog.component.html',
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
            private eventManager: EventManager,
    ) {
        this.jhiLanguageService.addLocation('role');
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res;
        });
        this.projectService.query().subscribe((res: HttpResponse<any>) => {
            this.projects = res.body;
        });

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
        this.eventManager.broadcast({name: 'roleListModification', content: 'OK'});
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
}

@Component({
    selector: 'jhi-source-data-popup',
    template: '',
})
export class RolePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
            private route: ActivatedRoute,
            private rolePopupService: RolePopupService,
    ) {
    }

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.modalRef = this.rolePopupService
                    .open(RoleDialogComponent, params['projectName'], params['authorityName']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
