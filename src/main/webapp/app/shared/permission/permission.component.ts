import {
    Component, Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import {Role} from "../../admin/user-management/role.model";
import {Subscription} from "rxjs";
import {Project, ProjectService} from "../project";
import {AuthorityService} from "../user/authority.service";
import {AlertService} from "../util/alert.service";
import {EventManager} from "../util/event-manager.service";
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {User} from "../user/user.model";
import {Organization} from "../organization";
import {UserService} from "../user/user.service";
import {parseLinks} from "../util/parse-links-util";

@Component({
    selector: 'jhi-permissions',
    templateUrl: './permission.component.html',
    styleUrls: ['./permission.component.scss'],
})
export class PermissionComponent implements OnInit, OnDestroy, OnChanges {


    @Input() organization: Organization;

    users: User[] = [];
    allUsers: User[];

    eventSubscriber: Subscription;
    authorities: string[];
    // projects: Project[];

    selectedAuthority: any;
    selectedUser: User;

    constructor(
            private authorityService: AuthorityService,
            // private projectService: ProjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private userService: UserService,
    ) {
    }

    ngOnInit() {

        // if (this.users === null) {
        //     this.users = [];
        // }
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res;
        });
        // this.projectService.query().subscribe((res: HttpResponse<any>) => {
        //     this.projects = res.body;
        // });
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        // update user

        // const newUser = new User();
        // newUser
        // const newRole = new Role();
        // newRole.authorityName = this.selectedAuthority;
        // newRole.projectId = this.selectedProject.id;
        // newRole.projectName = this.selectedProject.projectName;
        // if (this.hasRole(newRole)) {
        //     this.alertService.error('userManagement.role.error.alreadyExist', null, null);
        // } else {
        //     this.users.push(newRole);
        // }
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    hasRole(role: Role): boolean {
        return false;
        // this.users.some(v => v.projectId === role.projectId &&
        //         v.authorityName === role.authorityName);
    }

    removeRole(role: Role) {
        // this.users.splice(this.users.findIndex(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.userService.query({
            // page: this.page - 1,
            // size: this.itemsPerPage,
            // authority: this.byAuthority,
            // email: this.byEmail,
            // login: this.byLogin,
            organization: this.organization
            // projectName: this.byProject,
            // sort: this.sort(),
        }).subscribe(
                (res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res),
        );
    }

    ngOnDestroy(): void {
    }

    private onSuccess(data, headers) {
        // this.links = parseLinks(headers.get('link'));
        // this.totalItems = headers.get('X-Total-Count');
        // this.queryCount = this.totalItems;
        this.allUsers = data;
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

}
