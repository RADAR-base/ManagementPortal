import {
    Component,
    Input,
    OnDestroy,
    OnInit,
} from '@angular/core';

import { Project, User, UserService } from '..';
import { EventManager } from '../util/event-manager.service';
import { BehaviorSubject, combineLatest, Subject, Subscription } from "rxjs";
import {
    distinctUntilChanged,
    filter,
    first,
    map,
    pluck,
    startWith,
    switchMap,
    tap
} from "rxjs/operators";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
    selector: 'jhi-common-user-mgmt',
    templateUrl: './common-user-management.component.html'
})
export class CommonUserMgmtComponent implements OnInit, OnDestroy {
    readonly users$ = new BehaviorSubject<User[]>([]);
    predicate$ = new BehaviorSubject('id');
    ascending$ = new BehaviorSubject(true);

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
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.subscriptions.add(this.registerRouteParams());
    }

    ngOnInit() {
        this.subscriptions.add(this.registerChangeInUsers());
        this.subscriptions.add(this.registerUserEvents());
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    private registerRouteParams() {
        return this.activatedRoute.data.pipe(
            pluck('pagingParams'),
            first(),
        ).subscribe(params => {
            this.ascending$.next(params.ascending);
            this.predicate$.next(params.predicate);
        });
    }

    private registerChangeInUsers() {
        const sort$ = combineLatest([
            this.predicate$,
            this.ascending$,
        ]).pipe(
            map(([predicate, ascending]) => predicate + ',' + (ascending ? 'asc' : 'desc')),
            distinctUntilChanged(),
        );

        return combineLatest([
            this.project$.pipe(filter(p => !!p), pluck('projectName'), distinctUntilChanged()),
            this.authority$.pipe(filter(a => !!a), distinctUntilChanged()),
            sort$,
            this.trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
            tap(([, , sort]) => {
                this.router.navigate([], {
                    relativeTo: this.activatedRoute,
                    queryParams: { sort },
                })
            }),
            switchMap(([projectName, authority, sort]) => this.userService.findByProjectAndAuthority({
                projectName: projectName,
                authority,
                sort,
            })),
        ).subscribe((res: any) => this.users$.next(res));
    }

    private registerUserEvents() {
        return this.eventManager.subscribe('userListModification',
            () => this.trigger$.next(),
        );
    }

    trackIdentity(index, item: User) {
        return item.id;
    }

    transition() {
        this.trigger$.next();
    }
}
