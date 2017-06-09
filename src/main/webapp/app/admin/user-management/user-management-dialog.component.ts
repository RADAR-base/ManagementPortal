import {Component, OnInit, OnDestroy, OnChanges, SimpleChanges} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { UserModalService } from './user-modal.service';
import { JhiLanguageHelper, User, UserService } from '../../shared';
import { Project, ProjectService} from '../../entities/project';
import {Role} from "../../entities/role/role.model";
import {RoleService} from "../../entities/role/role.service";
import {Principal} from "../../shared/auth/principal.service";

@Component({
    selector: 'jhi-user-mgmt-dialog',
    templateUrl: './user-management-dialog.component.html'
})
export class UserMgmtDialogComponent implements OnInit {

    user: User;
    languages: any[];
    roles: Role[];
    defaultRoles: Role[];
    isSaving: Boolean;
    projects: Project[];
    currentAccount: any;

    constructor(
        public activeModal: NgbActiveModal,
        private languageHelper: JhiLanguageHelper,
        private jhiLanguageService: JhiLanguageService,
        private userService: UserService,
        private roleService: RoleService,
        private projectService: ProjectService,
        private principal: Principal,
        private eventManager: EventManager
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.roleService.findAdminRoles().subscribe((res) => {
           this.defaultRoles = res.json();
        });
        this.roles = this.defaultRoles;
        // this.authorities = this.authorityService.findAll();
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
        this.jhiLanguageService.setLocations(['user-management']);

        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            } );

        this.principal.identity().then((account) => {
            this.currentAccount = account;
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
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    trackRoleById(index: number, item: Role) {
        return item.id;
    }

    public onProjectChange(project: any) {
        console.log("changed " , project);
        if(project==null) {
           this.roles = this.defaultRoles;
        }
        else {
            this.roleService.findByProject(project.id).subscribe((res) => {
                this.roles = res.json();
            });
        }
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
            if ( params['login'] ) {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent, params['login']);
            } else {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
