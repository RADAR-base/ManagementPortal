import { ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, } from '@angular/core';
import { Role } from '../../admin/user-management/role.model';
import { combineLatest, Observable, of, Subscription } from 'rxjs';
import { Project } from '../project';
import { AuthorityService } from '../user/authority.service';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { User } from '../user/user.model';
import { Organization } from '../organization';
import { UserService } from '../user/user.service';
import { Authority, Scope } from '../user/authority.model';
import { distinctUntilChanged, first, map, switchMap, tap } from 'rxjs/operators';
import { Principal } from '../auth/principal.service';

interface UserRole {
    login: string;
    authorityName: string;
}

@Component({
    selector: 'jhi-permissions',
    templateUrl: './permission.component.html',
    styleUrls: ['./permission.component.scss'],
})
export class PermissionComponent implements OnInit, OnChanges, OnDestroy {
    @Input() organization: Organization;
    @Input() project: Project;

    users: User[] = [];
    allUsers: User[] = [];
    authorizedUsers: UserRole[] = [];
    authorities$: Observable<Authority[]>

    selectedAuthority: Authority;
    selectedUser: User;

    private subscriptions: Subscription = new Subscription();

    constructor(
            private authorityService: AuthorityService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private userService: UserService,
            private changeDetectorRef: ChangeDetectorRef,
            public principal: Principal,
    ) {}

    ngOnInit() {
        this.authorities$ = this.authorityService.authorities$.pipe(
          map(authorities => {
              let scope: Scope;
              if (this.organization) {
                  scope = Scope.ORGANIZATION;
              } else if (this.project) {
                  scope = Scope.PROJECT;
              } else {
                  scope = Scope.GLOBAL;
              }
              return authorities.filter(a => a.scope === scope);
          }),
          distinctUntilChanged((a1, a2) => a1.map(a => a.name).join(',') === a2.map(a => a.name).join(',')),
          tap(a => {
              if (a.length > 0) {
                  this.selectedAuthority = a[0]
              } else {
                  this.selectedAuthority = undefined;
              }
          })
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.getUsers();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    trackId(index: number, item: Role) {
        return item.id;
    }

    getUsers() {
        this.selectedUser = null;
        this.subscriptions.add(this.fetchUsers().subscribe());
    }

    private fetchUsers(): Observable<any> {
        return this.userService.query({includeProvenance: false}).pipe(
            tap(
                (res: HttpResponse<User[]>) => this.onSuccess(res.body),
                (error: HttpErrorResponse) => this.onError(error),
            ),
        );
    }

    private onSuccess(data: User[]) {
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
        if (!this.selectedAuthority) {
            return;
        }
        let newRole: Role;
        if (this.organization) {
            newRole = {
                authorityName: this.selectedAuthority.toString(),
                organizationId: this.organization.id,
                organizationName: this.organization.name,
            };
        }
        if (this.project) {
            newRole = {
                authorityName: this.selectedAuthority.toString(),
                projectId: this.project.id,
                projectName: this.project.projectName,
            };
        }
        const user = this.selectedUser;
        user.roles.push(newRole);
        user.authorities.push(this.selectedAuthority.name)
        this.subscriptions.add(this.updateUser(user));
    }

    removeRole(login: string, authorityName: string) {
        const user = this.allUsers.find(user => user.login === login);
        const roles = user.roles;
        if (this.organization) {
            roles.splice(roles.findIndex(role => role.authorityName === authorityName && role.organizationId === this.organization.id), 1);
        }
        if (this.project) {
            roles.splice(roles.findIndex(role => role.authorityName === authorityName && role.projectId === this.project.id), 1);
        }
        user.roles = roles;
        user.authorities = [...new Set(roles.map(a => a.authorityName))];

        this.subscriptions.add(this.updateUser(user));
    }

    private updateUser(user: User): Subscription {
        return this.userService.update(user).pipe(
            tap(() => this.selectedUser = null),
            switchMap(() => combineLatest([
                this.principal.account$.pipe(
                    first(),
                    switchMap(account => {
                        if (account.id === user.id) {
                            return this.principal.reset();
                        } else {
                            return of(account);
                        }
                    }),
                ),
                this.fetchUsers(),
            ])),
        ).subscribe()
    }

    filterByProjectOrOrganization(users: User[]): { users: User[], authorizedUsers: UserRole[] } {
        const result = {
            users: [],
            authorizedUsers: [],
        }
        users.map(user => {
            let userAdded = false;
            user.roles.map(role => {
                if (this.project) {
                    if ( role.projectId === this.project.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                        userAdded = true;
                        result.authorizedUsers.push({login: user.login, authorityName: role.authorityName})
                    }
                }
                if (this.organization) {
                    if (role.organizationId === this.organization.id || role.authorityName === 'ROLE_SYS_ADMIN') {
                        userAdded = true;
                        result.authorizedUsers.push({login: user.login, authorityName: role.authorityName})
                    }
                }
            })
            if (!userAdded) {
                result.users.push(user)
            }
        });

        return result;
    }
}
