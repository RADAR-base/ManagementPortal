import { ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, } from '@angular/core';
import { Role } from '../../admin/user-management/role.model';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { Project } from '../project';
import { AuthorityService } from '../user/authority.service';
import { AlertService } from '../util/alert.service';
import { EventManager } from '../util/event-manager.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { User } from '../user/user.model';
import { Organization } from '../organization';
import { UserService } from '../user/user.service';
import { Authority, Scope } from '../user/authority.model';
import { distinctUntilChanged, map, switchMap, tap } from 'rxjs/operators';

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
            private changeDetectorRef: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.authorities$ = this.authorityService.authorities$.pipe(
          map(authorities => {
              if (this.organization) {
                  return authorities.filter(a => a.scope === Scope.ORGANIZATION);
              } else if (this.project) {
                  return authorities.filter(a => a.scope === Scope.PROJECT);
              } else {
                  return authorities.filter(a => a.scope === Scope.GLOBAL);
              }
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
                authorityName: this.selectedAuthority.name,
                organizationId: this.organization.id,
                organizationName: this.organization.name,
            };
        }
        if (this.project) {
            newRole = {
                authorityName: this.selectedAuthority.name,
                projectId: this.project.id,
                projectName: this.project.projectName,
            };
        }
        this.selectedUser.roles.push(newRole);
        this.selectedUser.authorities.push(this.selectedAuthority.name)
        this.subscriptions.add(this.userService.update(this.selectedUser).pipe(
            switchMap(() => {
                this.selectedUser = null;
                return this.fetchUsers();
            }),
        ).subscribe());
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

        this.subscriptions.add(this.userService.update(user).pipe(
            switchMap(() => {
                if (this.selectedUser && this.selectedUser.login === login) {
                    this.selectedUser = null;
                }
                return this.fetchUsers();
            }),
        ).subscribe());
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
