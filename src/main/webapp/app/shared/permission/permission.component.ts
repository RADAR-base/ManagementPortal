import {
    Component, Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import {Role} from "../../admin/user-management/role.model";
import {Observable, of, OperatorFunction, Subscription} from "rxjs";
import {Project} from "../project";
import {AuthorityService} from "../user/authority.service";
import {AlertService} from "../util/alert.service";
import {EventManager} from "../util/event-manager.service";
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {User} from "../user/user.model";
import {Organization} from "../organization";
import {UserService} from "../user/user.service";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";

const MOCK_USER_DATA = [ {
    "id" : 1,
    "login" : "admin",
    "firstName" : "Administrator",
    "lastName" : "Administrator",
    "email" : "admin@localhost",
    "activated" : true,
    "langKey" : "en",
    "createdBy" : "system",
    "createdDate" : "2021-11-19T10:08:30.968406+01:00",
    "lastModifiedBy" : "system",
    "lastModifiedDate" : "2021-11-19T10:08:40.261+01:00",
    "roles" : [ {
        "id" : 1,
        "projectId" : null,
        "projectName" : null,
        "authorityName" : "ROLE_SYS_ADMIN"
    } ],
    "authorities" : [ "ROLE_SYS_ADMIN" ]
}, {
    "id" : 5,
    "login" : "padmin",
    "firstName" : null,
    "lastName" : null,
    "email" : "padmin@localhost",
    "activated" : true,
    "langKey" : "en",
    "createdBy" : "system",
    "createdDate" : "2021-11-19T10:08:30.968406+01:00",
    "lastModifiedBy" : "system",
    "lastModifiedDate" : "2021-11-19T10:08:30.968406+01:00",
    "roles" : [ {
        "id" : 3,
        "projectId" : 1,
        "projectName" : "radar",
        "authorityName" : "ROLE_PROJECT_ADMIN"
    }, {
        "id" : 5,
        "organizationId" : 1,
        "organizationName" : "The Hyve",
        "authorityName" : "ROLE_ORGANIZATION_ADMIN"
    }  ],
    "authorities" : [ "ROLE_PROJECT_ADMIN", "ROLE_ORGANIZATION_ADMIN" ]
}, {
    "id" : 6,
    "login" : "padmin2",
    "firstName" : null,
    "lastName" : null,
    "email" : "padmin2@localhost",
    "activated" : true,
    "langKey" : "en",
    "createdBy" : "system",
    "createdDate" : "2021-11-19T10:08:30.968406+01:00",
    "lastModifiedBy" : "system",
    "lastModifiedDate" : "2021-11-19T10:08:30.968406+01:00",
    "roles" : [ {
        "id" : 4,
        "projectId" : 2,
        "projectName" : "Radar-Pilot-01",
        "authorityName" : "ROLE_PROJECT_ADMIN"
    }, {
        "id" : 6,
        "organizationId" : 2,
        "organizationName" : "Other organization",
        "authorityName" : "ROLE_ORGANIZATION_ADMIN"
    }   ],
    "authorities" : [ "ROLE_PROJECT_ADMIN", "ROLE_ORGANIZATION_ADMIN" ]
} ];

@Component({
    selector: 'jhi-permissions',
    templateUrl: './permission.component.html',
    styleUrls: ['./permission.component.scss'],
})
export class PermissionComponent implements OnInit, OnDestroy, OnChanges {


    @Input() organization: Organization;
    @Input() project: Project;

    authorities: string[];

    users: User[] = [];
    filteredUsers: User[] = [];

    eventSubscriber: Subscription;

    selectedAuthority: any;
    userSearchTerm: any;

    isSearching = false;
    isSearchFailed = false;
    isNotSelected = false;

    searchUser: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) =>
            text$.pipe(
                    debounceTime(300),
                    distinctUntilChanged(),
                    tap(() => this.isSearching = true),
                    switchMap(term =>
                            this.userService.search(term).pipe(
                                    map( users => this.removeAlreadyExistingUsersFromTheList(users)),
                                    tap(() => this.isSearchFailed = false),
                                    catchError(() => {
                                        this.isSearchFailed = true;
                                        return of([]);
                                    }))
                    ),
                    tap(() => this.isSearching = false)
            )

    constructor(
            private authorityService: AuthorityService,
            // private projectService: ProjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private userService: UserService,
    ) {
    }

    removeAlreadyExistingUsersFromTheList(users: User[]){
        const modifiedUsers = this.users.map(u => u.login);
        this.filteredUsers = users.filter(val => !modifiedUsers.includes(val.login));
        return this.filteredUsers.map(user => {
            const userNameString = (user.firstName || user.lastName)? ' [' + (user.firstName || '') + ' ' + (user.lastName || '') + ']' : '';
            return user.login + userNameString;
        });
    }

    ngOnInit() {
        this.authorityService.findAll().subscribe(res => {
            this.authorities = res;
        });
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    addRole() {
        console.log(this.selectedAuthority, this.userSearchTerm)
        console.log(this.filteredUsers)
        const user = this.filteredUsers[0];
        if(this.userSearchTerm.includes(user.login)){
            let newRole: Role;
            if (this.project) {
                newRole = {
                    authorityName: 'ROLE_PROJECT_ADMIN',
                    projectId: 1, //this.organization.id,
                    projectName: 'radar', // this.organization.organizationName,
                };
            } else if (this.organization) {
                newRole = {
                    authorityName: this.selectedAuthority,
                    organizationId: this.organization.id,
                    organizationName: this.organization.name,
                };
            } else {
                return;
            }
            if (this.hasRole(newRole)) {
            //     this.alertService.error('userManagement.role.error.alreadyExist', null, null);
            } else {
                user.roles.push(newRole);
                this.userService.update(user).subscribe(
                        (res)=> {
                            console.log(res);
                            this.getUsers();
                        },
                        (error)=> console.log(error)
                );
            //     this.users.push(newRole);
            }
        } else {
            console.log('error not selected')
            this.isNotSelected = true;
        }
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    getUsers() {
        this.selectedAuthority = null;
        this.userSearchTerm = null;
        this.isNotSelected = false;
        this.isSearching = false;
        this.isSearchFailed = false;

        let req = {};
        if (this.project) {
            req = {
                projectName: this.project.projectName
            };
        } else if (this.organization) {
            req = {
                organization: this.organization.name
            };
        } else {
            return;
        }

        this.userService.query(req).subscribe(
                (res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers),
                (error: HttpErrorResponse) => this.onError(error),
        );
    }

    hasRole(role: Role): boolean {
        return false;
        // this.users.some(v => v.projectId === role.projectId &&
        //         v.authorityName === role.authorityName);
    }

    removeRole(user: User) {
        // remove from roles
        const roles = user.roles;
        console.log(roles);
        roles.splice(roles.findIndex(role => role.organizationId === this.organization.id), 1);
        user.roles = roles;

        // remove from authorities
        // todo warning! if user has more than one authority for organization or project
        const authorities = user.authorities;
        console.log(authorities);
        authorities.splice(authorities.findIndex(authority => authority === this.selectedAuthority), 1);
        user.authorities = authorities;

        this.userService.update(user).subscribe(
                (res)=> {
                    console.log(res);
                    this.getUsers();
                },
                (error)=> console.log(error)
        );
        // this.users.splice(this.users.findIndex(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    trackProjectById(index: number, item: Project) {
        return item.id;
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.getUsers();
    }

    ngOnDestroy(): void {
    }

    private onSuccess(data, headers) {
        const organizationUsers = this.filterByOrganization(data);
        console.log(organizationUsers);
        this.users = this.filterByOrganization(organizationUsers);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    filterByOrganization(data: any[]): any[] {
        return data.filter( item => {
            return item.authorities.includes('ROLE_SYS_ADMIN') ||
                    item.roles.filter(role => role.organizationId === 1).length > 0;
        })
    }

}
