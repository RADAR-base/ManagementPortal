import {
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges
} from '@angular/core';

import { ITEMS_PER_PAGE, Project, User, UserService } from '..';
import { EventManager } from '../util/event-manager.service';
import { BehaviorSubject, combineLatest, Subject, Subscription } from "rxjs";
import { filter, startWith, switchMap } from "rxjs/operators";

@Component({
    selector: 'jhi-common-user-mgmt',
    templateUrl: './common-user-management.component.html'
})
export class CommonUserMgmtComponent implements OnInit, OnChanges, OnDestroy {
    users: User[];
    error: any;
    success: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    reverse: any;

    project$ = new BehaviorSubject<Project>(null);
    @Input()
    get project(): Project { return this.project$.value; }
    set project(v: Project) { this.project$.next(v) }

    authority$ = new BehaviorSubject<string>('');
    @Input()
    get authority(): string { return this.authority$.value; }
    set authority(v: string) { this.authority$.next(v); }

    trigger$ = new Subject<void>();


    private subscriptions: Subscription = new Subscription();

    constructor(
            private userService: UserService,
            private eventManager: EventManager,
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
    }

    ngOnInit() {
        this.subscriptions.add(
            combineLatest([
              this.project$,
              this.authority$,
              this.trigger$.pipe(startWith(undefined as void)),
            ]).pipe(
              filter(([p, a]) => (!!p) && (!!a)),
              switchMap(([project, authority]) => this.userService.findByProjectAndAuthority({
                  projectName: project.projectName,
                  authority: authority,
              })),
            ).subscribe((res: any) => this.users = res)
        );
        this.registerChangeInUsers();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    registerChangeInUsers() {
        this.subscriptions.add(
          this.eventManager.subscribe('userListModification', () => this.trigger$.next())
        );
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.project) {
            this.project$.next(changes.project.currentValue);
        }
        if (changes.authority) {
            this.authority$.next(changes.authority.currentValue);
        }
    }

    trackIdentity(index, item: User) {
        return item.id;
    }

    transition() {
        this.trigger$.next();
    }
}
