import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';

import { Account, Principal, Project, ProjectService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { distinctUntilChanged, filter, map, pluck, shareReplay, startWith, switchMap } from 'rxjs/operators';
import { SiteSettingsService} from "../../shared/subject";
import {HideableSubjectField, SiteSettings} from "../../shared/subject/sitesettings.service";

interface TabOptions {
    active: string | null;
    available: string[];
}

@Component({
    selector: 'jhi-project-detail',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
    private subscription = new Subscription();
    project$: Observable<Project>
    tab$: Observable<TabOptions>;
    public siteSettings$: Observable<SiteSettings>;

    private _activeTab$ = new BehaviorSubject('subjects');

    constructor(
            private eventManager: EventManager,
            private projectService: ProjectService,
            private activatedRoute: ActivatedRoute,
            private siteSettingsService: SiteSettingsService,
            private router: Router,
            public principal: Principal,
    ) {
        this.project$ = this.observeProject();
        this.tab$ = this.observeTab(this.project$);
    }

    ngOnInit() {
        this.subscription.add(this.activatedRoute.queryParams.pipe(
            pluck('tab'),
            filter(tab => !!tab),
        ).subscribe(tab => this.updateActiveTab(tab)));

        this.subscription.add(this.registerTabChange());
        this.siteSettings$ = this.siteSettingsService.siteSettings$;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this._activeTab$.complete();
    }

    private registerTabChange(): Subscription {
        return this.tab$.pipe(
            pluck('active'),
            filter(activeTab => activeTab !== null),
            distinctUntilChanged(),
            map((activeTab, index) => ({activeTab, index})),
        ).subscribe(({activeTab, index}) => this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: { tab: activeTab },
            queryParamsHandling: "merge",
            // Don't store the first iteration, where the active tab is not in the URL yet.
            replaceUrl: index === 0,
        }));
    }

    private observeProject(): Observable<Project> {
        return this.activatedRoute.params.pipe(
            filter(p => !!p),
            pluck('projectName'),
            distinctUntilChanged(),
            switchMap(projectName => this.projectService.find(projectName)),
            shareReplay(1),
        );
    }

    private observeTab(project$: Observable<Project>): Observable<TabOptions> {
        return combineLatest([
            project$,
            this.principal.account$,
            this._activeTab$,
        ]).pipe(
            map(([project, account, active]) => {
                const available = this.availableTabs(project, account);
                return {
                    available,
                    active: ProjectDetailComponent.firstAvailableTab(active, available),
                }
            }),
            startWith({active: null, available: []}),
            distinctUntilChanged((a, b) => a.active === b.active && a.available.join(' ') === b.available.join(' ')),
        );
    }

    private availableTabs(project: Project, account: Account): string[] {
        if (project && this.principal.accountHasAnyAuthority(account, ['ROLE_SYS_ADMIN', 'ROLE_ORGANIZATION_ADMIN:' + project.organization.name, 'ROLE_PROJECT_ADMIN:' + project.projectName])) {
            return ['subjects', 'sources', 'groups', 'permissions', 'admins', 'analysts'];
        } else {
            return [];
        }
    }

    private static firstAvailableTab(tab: string, available: string[]): string {
        if (available.length === 0) {
            return null;
        } else if (available.includes(tab)) {
            return tab;
        } else {
            return available[0];
        }
    }

    previousState() {
        window.history.back();
    }

    updateActiveTab(tab?: string) {
        this._activeTab$.next(tab);
    }

    protected readonly HideableSubjectField = HideableSubjectField;
}
