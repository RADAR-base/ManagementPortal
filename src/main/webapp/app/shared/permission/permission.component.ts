import {
    ChangeDetectorRef,
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

// const MOCK_USER_DATA = [ {
//     "id" : 1,
//     "login" : "admin",
//     "firstName" : "Administrator",
//     "lastName" : "Administrator",
//     "email" : "admin@localhost",
//     "activated" : true,
//     "langKey" : "en",
//     "createdBy" : "system",
//     "createdDate" : "2021-11-19T10:08:30.968406+01:00",
//     "lastModifiedBy" : "system",
//     "lastModifiedDate" : "2021-11-19T10:08:40.261+01:00",
//     "roles" : [ {
//         "id" : 1,
//         "projectId" : null,
//         "projectName" : null,
//         "authorityName" : "ROLE_SYS_ADMIN"
//     } ],
//     "authorities" : [ "ROLE_SYS_ADMIN" ]
// }, {
//     "id" : 5,
//     "login" : "padmin",
//     "firstName" : null,
//     "lastName" : null,
//     "email" : "padmin@localhost",
//     "activated" : true,
//     "langKey" : "en",
//     "createdBy" : "system",
//     "createdDate" : "2021-11-19T10:08:30.968406+01:00",
//     "lastModifiedBy" : "system",
//     "lastModifiedDate" : "2021-11-19T10:08:30.968406+01:00",
//     "roles" : [ {
//         "id" : 3,
//         "projectId" : 1,
//         "projectName" : "radar",
//         "authorityName" : "ROLE_PROJECT_ADMIN"
//     }, {
//         "id" : 5,
//         "organizationId" : 1,
//         "organizationName" : "The Hyve",
//         "authorityName" : "ROLE_ORGANIZATION_ADMIN"
//     }  ],
//     "authorities" : [ "ROLE_PROJECT_ADMIN", "ROLE_ORGANIZATION_ADMIN" ]
// }, {
//     "id" : 6,
//     "login" : "padmin2",
//     "firstName" : null,
//     "lastName" : null,
//     "email" : "padmin2@localhost",
//     "activated" : true,
//     "langKey" : "en",
//     "createdBy" : "system",
//     "createdDate" : "2021-11-19T10:08:30.968406+01:00",
//     "lastModifiedBy" : "system",
//     "lastModifiedDate" : "2021-11-19T10:08:30.968406+01:00",
//     "roles" : [ {
//         "id" : 4,
//         "projectId" : 2,
//         "projectName" : "Radar-Pilot-01",
//         "authorityName" : "ROLE_PROJECT_ADMIN"
//     }, {
//         "id" : 6,
//         "organizationId" : 2,
//         "organizationName" : "Other organization",
//         "authorityName" : "ROLE_ORGANIZATION_ADMIN"
//     }   ],
//     "authorities" : [ "ROLE_PROJECT_ADMIN", "ROLE_ORGANIZATION_ADMIN" ]
// } ];

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

    // filteredUsers: User[] = [];

    eventSubscriber: Subscription;

    selectedAuthority: any;
    selectedUser: any;
    // userSearchTerm: any;

    // isSearching = false;
    // isSearchFailed = false;
    // isNotSelected = false;

    // searchUser: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) =>
    //         text$.pipe(
    //                 debounceTime(300),
    //                 distinctUntilChanged(),
    //                 tap(() => this.isSearching = true),
    //                 switchMap(term =>
    //                         this.userService.search(term).pipe(
    //                                 map( users => this.removeAlreadyExistingUsersFromTheList(users)),
    //                                 tap(() => this.isSearchFailed = false),
    //                                 catchError(() => {
    //                                     this.isSearchFailed = true;
    //                                     return of([]);
    //                                 }))
    //                 ),
    //                 tap(() => this.isSearching = false)
    //         )

    constructor(
            private authorityService: AuthorityService,
            // private projectService: ProjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private userService: UserService,
            private changeDetectorRef: ChangeDetectorRef
    ) {
    }

    // removeAlreadyExistingUsersFromTheList(users: User[]){
    //     const modifiedUsers = this.users.map(u => u.login);
    //     this.filteredUsers = users.filter(val => !modifiedUsers.includes(val.login));
    //     return this.filteredUsers.map(user => {
    //         const userNameString = (user.firstName || user.lastName)? ' [' + (user.firstName || '') + ' ' + (user.lastName || '') + ']' : '';
    //         return user.login + userNameString;
    //     });
    // }

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

    // trackProjectById(index: number, item: Project) {
    //     return item.id;
    // }

    getUsers() {
        // this.selectedAuthority = null;
        this.selectedUser = null;

        // let req = {};
        // if (this.project) {
        //     req = {
        //         projectName: this.project.projectName
        //     };
        // } else if (this.organization) {
        //     req = {
        //         organization: this.organization.name
        //     };
        // } else {
        //     return;
        // }

        this.userService.query().subscribe(
            (res: HttpResponse<User[]>) => this.onSuccess(res.body, res.headers),
            (error: HttpErrorResponse) => this.onError(error),
        );
    }

    private onSuccess(data, headers) {
        this.allUsers = data;
        if (this.organization) {
            const {authorizedUsers, users} = this.filterByOrganization(data);
            this.authorizedUsers = authorizedUsers; //this.filterByProject(data);
            this.users = users;
            // this.authorizedUsers = this.filterByOrganization(data);
        }
        if (this.project) {
            const {authorizedUsers, users} = this.filterByProject(data);
            this.authorizedUsers = authorizedUsers; //this.filterByProject(data);
            this.users = users;
        }
        // this.users = this.removeAuthorizedUsers(data, this.authorizedUsers);
        // this.users = this.removeSysAdmin(data);
        this.changeDetectorRef.detectChanges();
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    hasRole(role: Role): boolean {
        return false;
        // this.users.some(v => v.projectId === role.projectId &&
        //         v.authorityName === role.authorityName);
    }

    addRole() {
        let newRole: Role;
        if (this.organization) {
            newRole = {
                authorityName: this.selectedAuthority,
                organizationId: this.organization.id,
                organizationName: this.organization.name,
            };
            if (this.hasRole(newRole)) {
                //     this.alertService.error('userManagement.role.error.alreadyExist', null, null);
            } else {
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

        }

        if (this.project) {
            newRole = {
                authorityName: this.selectedAuthority,
                projectId: this.project.id,
                projectName: this.project.projectName,
            };
            if (this.hasRole(newRole)) {
                //     this.alertService.error('userManagement.role.error.alreadyExist', null, null);
            } else {
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
        }
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
        // this.users.splice(this.users.findIndex(v => v.projectId === role.projectId && v.authorityName === role.authorityName), 1);
        // this.eventManager.broadcast({name: 'roleListModification', content: this.users});
    }

    // filterByOrganization(users: any[]): any[] {
    //     const output = [];
    //     users.map(user => {
    //         user.roles.map(role => {
    //             if ( role.organizationId === this.organization.id || role.authorityName === 'ROLE_SYS_ADMIN') {
    //                 output.push({login: user.login, authorityName: role.authorityName})
    //             }
    //         })
    //     })
    //
    //     return output;
    //     // return data.filter( item => {
    //     //     return item.authorities.includes('ROLE_SYS_ADMIN') ||
    //     //             item.roles.filter(role => role.organizationId === this.organization.id).length > 0;
    //     // })
    // }
    filterByOrganization(users: any[]): any {
        const usersOutput = [];
        const authorizedUsersOutput = [];
        users.map(user => {
            let userAdded = false;
            user.roles.map(role => {
                if ( role.organizationId === this.organization.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                    userAdded = true;
                    authorizedUsersOutput.push({login: user.login, authorityName: role.authorityName})
                }
            })
            if (!userAdded) {
                usersOutput.push(user)
            }
        });
        console.log(usersOutput, authorizedUsersOutput);

        return {users: usersOutput, authorizedUsers: authorizedUsersOutput};

        // return users.filter(user => {
        //     return user.roles.filter(role => (role.projectId === this.project.id) || (!role.projectId && role.authorityName === 'ROLE_SYS_ADMIN')).length > 0;
        // })
        // return data.filter( item => {
        //     return item.authorities.includes('ROLE_SYS_ADMIN') ||
        //         item.roles.filter(role => role.projectId === this.project.id).length > 0;
        // })
    }

    filterByProject(users: any[]): any {
        const usersOutput = [];
        const authorizedUsersOutput = [];
        users.map(user => {
            let userAdded = false;
            user.roles.map(role => {
                if ( role.projectId === this.project.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                    userAdded = true;
                    authorizedUsersOutput.push({login: user.login, authorityName: role.authorityName})
                }
            })
            if (!userAdded) {
                usersOutput.push(user)
            }
        });
        console.log(usersOutput, authorizedUsersOutput);

        return {users: usersOutput, authorizedUsers: authorizedUsersOutput};

        // return users.filter(user => {
        //     return user.roles.filter(role => (role.projectId === this.project.id) || (!role.projectId && role.authorityName === 'ROLE_SYS_ADMIN')).length > 0;
        // })
        // return data.filter( item => {
        //     return item.authorities.includes('ROLE_SYS_ADMIN') ||
        //         item.roles.filter(role => role.projectId === this.project.id).length > 0;
        // })
    }

    // removeAuthorizedUsers(users, authorizedUsers): User[] {
    //
    // }
    //
    // removeSysAdmin(users: User[]): User[] {
    //     console.log(users)
    //     return users.filter(user => !user.authorities.includes('ROLE_SYS_ADMIN'))
    // }

}
