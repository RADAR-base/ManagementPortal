import {Component, OnInit, OnDestroy, OnChanges, SimpleChanges} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { UserModalService } from './user-modal.service';
import { JhiLanguageHelper, User, UserService } from '../../shared';
import { Project, ProjectService} from '../../entities/project';
import {Role} from "../../entities/role/role.model";
import {RoleService} from "../../entities/role/role.service";
import {Principal} from "../../shared/auth/principal.service";
import {Authority} from "../../shared/user/authority.model";
import {AuthorityService} from "../../shared/user/authority.service";

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
    authorities: Authority[];

    constructor(
        public activeModal: NgbActiveModal,
        private languageHelper: JhiLanguageHelper,
        private jhiLanguageService: JhiLanguageService,
        private userService: UserService,
        private roleService: RoleService,
        private authorityService: AuthorityService,
        private projectService: ProjectService,
        // private principal: Principal,
        private eventManager: EventManager
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.authorityService.findAll().subscribe((res: Response) => {
            this.authorities = res.json();
        });
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
        this.jhiLanguageService.setLocations(['user-management']);

        this.projectService.query().subscribe(
            (res) => {
                this.projects = res.json();
            } );

        // this.principal.hasAuthority("ROLE_SYS_ADMIN").then((account) => {
        //     console.log("Is admin", account);
        //     this.roleService.findAdminRoles().subscribe((res: Response) => {
        //         this.defaultRoles = res.json();
        //         this.roles = this.defaultRoles;
        //         console.log("default role ", this.defaultRoles);
        //     });
        // });
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

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    trackRoleById(index: number, item: Role) {
        return item.id;
    }

    public onProjectChange(project: any) {
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
