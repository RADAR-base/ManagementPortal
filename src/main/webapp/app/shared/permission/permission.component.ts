import {
    ChangeDetectorRef,
    Component, Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import {Role} from "../../admin/user-management/role.model";
import {Subscription} from "rxjs";
import {Project} from "../project";
import {AuthorityService} from "../user/authority.service";
import {AlertService} from "../util/alert.service";
import {EventManager} from "../util/event-manager.service";
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {User} from "../user/user.model";
import {Organization} from "../organization";
import {UserService} from "../user/user.service";

@Component({
    selector: 'jhi-permissions',
    templateUrl: './permission.component.html',
    styleUrls: ['./permission.component.scss'],
})
export class PermissionComponent implements OnInit, OnDestroy, OnChanges {

    @Input() organization: Organization;
    @Input() project: Project;

    authorities: string[];

    allUsers: User[] = [];
    users: User[] = [];
    authorizedUsers: User[] = [];

    eventSubscriber: Subscription;

    selectedAuthority: any;
    selectedUser: any;

    constructor(
            private authorityService: AuthorityService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private userService: UserService,
            private changeDetectorRef: ChangeDetectorRef
    ) {}

    ngOnInit() {
        if (this.organization) {
            this.selectedAuthority = 'ROLE_ORGANIZATION_ADMIN'
        }
        if (this.project) {
            this.selectedAuthority = 'ROLE_PROJECT_ADMIN'
        }
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res;
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.getUsers();
    }

    ngOnDestroy(): void {}


    trackId(index: number, item: Role) {
        return item.id;
    }

    getUsers() {
        this.selectedUser = null;
        this.userService.query().subscribe(
            (res: HttpResponse<User[]>) => this.onSuccess(res.body),
            (error: HttpErrorResponse) => this.onError(error),
        );
    }

    private onSuccess(data) {
        this.allUsers = data;
        const {authorizedUsers, users} = this.filterByProjectOrOrganization(data);
        this.authorizedUsers = authorizedUsers;
        this.users = users;
        this.changeDetectorRef.detectChanges();
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    addRole() {
        let newRole: Role;
        if (this.organization) {
            newRole = {
                authorityName: this.selectedAuthority,
                organizationId: this.organization.id,
                organizationName: this.organization.name,
            };
        }
        if (this.project) {
            newRole = {
                authorityName: this.selectedAuthority,
                projectId: this.project.id,
                projectName: this.project.projectName,
            };
        }
        this.selectedUser.roles.push(newRole);
        this.selectedUser.authorities.push(this.selectedAuthority)
        this.userService.update(this.selectedUser).subscribe(
            (res) => {
                console.log(res);
                this.getUsers();
                // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
            },
            (error) => console.log(error)
        );
    }

    removeRole(login: string) {
        const user = this.allUsers.find(user => user.login === login);
        console.log(user)
        const roles = user.roles;
        if (this.organization) {
            roles.splice(roles.findIndex(role => role.organizationId === this.organization.id), 1);
        }
        if (this.project) {
            roles.splice(roles.findIndex(role => role.projectId === this.project.id), 1);
        }
        user.roles = roles;

        // todo warning! if user has more than one authority for organization or project
        const authorities = user.authorities;
        authorities.splice(authorities.findIndex(authority => authority === this.selectedAuthority), 1);
        user.authorities = authorities;

        this.userService.update(user).subscribe(
            (res)=> {
                this.getUsers();
            },
            (error)=> console.log(error)
        );
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    filterByProjectOrOrganization(users: any[]): any {
        const usersOutput = [];
        const authorizedUsersOutput = [];
        users.map(user => {
            let userAdded = false;
            user.roles.map(role => {
                if (this.project) {
                    if ( role.projectId === this.project.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                        userAdded = true;
                        authorizedUsersOutput.push({login: user.login, authorityName: role.authorityName})
                    }
                }
                if (this.organization) {
                    if ( role.organizationId === this.organization.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                        userAdded = true;
                        authorizedUsersOutput.push({login: user.login, authorityName: role.authorityName})
                    }
                }
            })
            if (!userAdded) {
                usersOutput.push(user)
            }
        });
        console.log(usersOutput, authorizedUsersOutput);

        return {users: usersOutput, authorizedUsers: authorizedUsersOutput};
    }
}
