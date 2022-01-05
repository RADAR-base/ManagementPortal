import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';

import {Principal, Project, ProjectService} from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { distinctUntilChanged, filter, pluck, switchMap } from "rxjs/operators";

@Component({
    selector: 'jhi-project-detail',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
    private static availableTabs = ['subjects', 'groups', 'permissions', 'admins', 'analysts']
    private subscription = new Subscription();
    private _project$ = new BehaviorSubject<Project>(null);
    project$ = this._project$.asObservable();

    private _activeTab$ = new BehaviorSubject('subjects');
    activeTab$: Observable<string>;

    constructor(
            private eventManager: EventManager,
            private projectService: ProjectService,
            private activatedRoute: ActivatedRoute,
            private router: Router,
            public principal: Principal,
    ) {
        this.activeTab$ = this._activeTab$.asObservable().pipe(distinctUntilChanged());
        this.subscription.add(this.activatedRoute.queryParams.pipe(
            pluck('tab'),
        ).subscribe(tab => this.updateActiveTab(tab)));
    }

    ngOnInit() {
        this.subscription.add(this.registerChangesInProjectName());
        this.subscription.add(this._activeTab$.subscribe(tab => this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: { tab },
            queryParamsHandling: "merge",
        })))
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this._project$.complete();
        this._activeTab$.complete();
    }

    private registerChangesInProjectName(): Subscription {
        return this.activatedRoute.params.pipe(
            filter(p => !!p),
            pluck('projectName'),
            distinctUntilChanged(),
            switchMap(projectName => this.projectService.find(projectName)),
        ).subscribe(project => this._project$.next(project));
    }

    previousState() {
        window.history.back();
    }

    updateActiveTab(tab?: string) {
        if (!tab) {
            return;
        }
        if (!ProjectDetailComponent.availableTabs.includes(tab)) {
            window.console.log(`Cannot load unknown tab ${tab}`);
            return;
        }
        this._activeTab$.next(tab);
    }
}
