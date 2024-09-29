import {Component, Input, OnDestroy, OnInit,} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, combineLatest, Observable, Subject, Subscription} from 'rxjs';

import {Group, GroupService, Project} from '..';
import {AlertService} from '../util/alert.service';
import {EventManager} from '../util/event-manager.service';
import {catchError, distinctUntilChanged, filter, pluck, startWith, switchMap} from "rxjs/operators";
import {Router} from "@angular/router";

@Component({
    selector: 'jhi-groups',
    templateUrl: './group.component.html',
    styleUrls: ['./group.component.scss'],
})
export class GroupComponent implements OnInit, OnDestroy {
    project$ = new BehaviorSubject<Project>(null);
    projectName$: Observable<string>;
    groups$ = new BehaviorSubject<Group[]>([]);
    private trigger$ = new Subject<void>();
    private subscription: Subscription = new Subscription();

    constructor(
        private groupService: GroupService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private router: Router,
    ) {
        this.projectName$ = this.project$.pipe(
            filter(p => !!p),
            pluck('projectName'),
            distinctUntilChanged(),
        );
    }

    @Input()
    get project() {
        return this.project$.value;
    }

    set project(v: Project) {
        this.project$.next(v);
    }

    ngOnInit() {
        this.subscription.add(this.registerChangeInGroups());
        this.subscription.add(this.registerChangeInGroupsEvents());
    }

    ngOnDestroy() {
        this.trigger$.complete();
        this.project$.complete();
        this.groups$.complete();
        this.subscription.unsubscribe();
    }

    trackId(index: number, item: Group) {
        return item.id;
    }

    registerChangeInGroups(): Subscription {
        return combineLatest([
            this.projectName$,
            this.trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
            switchMap(([projectName]) => this.groupService.list(projectName)),
            catchError((res: HttpErrorResponse) => {
                this.alertService.error(res.message, null, null);
                return [];
            }),
        ).subscribe(groups => this.groups$.next(groups));
    }

    registerChangeInGroupsEvents(): Subscription {
        return this.eventManager.subscribe('groupListModification', (_) => {
            this.trigger$.next();
        });
    }

    deleteGroup(group: Group) {
        this.groupService.delete(this.project.projectName, group.name).subscribe(
            () => {
                this.eventManager.broadcast({name: 'groupListModification', content: null});
            },
            (error) => {
                if (error.status === 409) {
                    return this.router.navigate(['/', {outlets: {popup: 'project-group/' + this.project.projectName + '/' + group.id + '/delete'}}])
                }
            }
        );
    }
}
