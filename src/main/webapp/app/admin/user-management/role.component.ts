import {Component, OnInit, OnDestroy, EventEmitter, Output, Input,} from '@angular/core';
import {
    EventManager,
    PaginationUtil,
    ParseLinks,
    AlertService,
    JhiLanguageService
} from 'ng-jhipster';

import {Role} from './role.model';
import {ITEMS_PER_PAGE, Principal} from '../../shared';
import {Subscription} from "rxjs/Subscription";
import {Project} from "../../entities/project/project.model";
import {AuthorityService} from "../../shared/user/authority.service";
import {ProjectService} from "../../entities/project/project.service";

@Component({
    selector: 'user-role',
    templateUrl: './role.component.html'
})
export class RoleComponent implements OnInit {
    @Input() roles: Role[] ;
    currentAccount: any;
    eventSubscriber: Subscription;
    authorities: any[];
    projects: Project[];

    selectedAuthority: any;
    selectedProject: Project;

    constructor(private jhiLanguageService: JhiLanguageService,
                private authorityService: AuthorityService,
                private projectService: ProjectService,
                private alertService: AlertService,
                private eventManager: EventManager,
                private principal: Principal) {
        this.jhiLanguageService.setLocations(['role']);
    }

    loadAll() {
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res.json();
        });
        this.projectService.query().subscribe((res) => {
            this.projects = res.json();
        });
    }

    ngOnInit() {
        if(this.roles ==null) {
            this.roles = new Array();
        }
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
    }

    registerChangeInRoles() {
        this.eventManager.subscribe('roleEditListModification', (response ) => {
            this.roles = response.content ;
        });
    }


    trackId(index: number, item: Role) {
        return item.id;
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    addRole() {
        let newRole = new Role();
        newRole.authorityName = this.selectedAuthority;
        newRole.projectId = this.selectedProject.id;
        newRole.projectName = this.selectedProject.projectName;
        if (this.hasRole(newRole)) {
            this.alertService.error('userManagement.role.error.alreadyExist', null, null);
        }
        else {
            this.roles.push(newRole);
        }
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    hasRole(role: Role): boolean {
        return this.roles.some(v => v.projectId === role.projectId &&
        v.authorityName === role.authorityName)
    }

    removeRole(role: Role) {
        this.roles.splice(this.roles.indexOf(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        this.eventManager.broadcast({name: 'roleListModification', content: this.roles});
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }
}
