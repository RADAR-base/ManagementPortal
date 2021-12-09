import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import { map } from 'rxjs/operators';

import {ITEMS_PER_PAGE, Organization, Project, ProjectService} from '../../shared';
import { PagingParams } from '../../shared/commons';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';

@Component({
    selector: 'jhi-projects',
    templateUrl: './project.component.html',
})
export class ProjectComponent implements OnInit, OnDestroy {
    pagingParams$: Observable<PagingParams>;

    organization$ = new BehaviorSubject<Organization>(null);

    @Input()
    get organization() { return this.organization$.value; }
    set organization(v: Organization) {
        this.organization$.next(v);
        this.loadAll();
    }

    projects: Project[];
    eventSubscriber: Subscription;
    itemsPerPage: number;
    page: any;
    predicate: any;
    queryCount: any;
    ascending: any;
    totalItems: number;
    routeData: any;
    previousPage: any;

    @Input() isOrganizationSpecific: boolean;

    constructor(
            private projectService: ProjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.projects = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.pagingParams$ = this.activatedRoute.data.pipe(map(data => {
            const fallback = { page: 1, predicate: 'id', ascending: true };
            return data['pagingParams'] || fallback;
        }));
        this.routeData = this.pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
    }

    loadAll() {
        this.projectService.findAllByOrganization(this.organization.name).subscribe({
            next: projects => this.projects = projects,
            error: error => this.alertService.error(error.message, null, null)
        })
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInProjects();

        this.pagingParams$.subscribe(() => {
            this.loadAll();
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    registerChangeInProjects() {
        this.eventSubscriber = this.eventManager.subscribe('projectListModification', () => this.loadAll());
    }

    sort() {
        const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/project'], {
            queryParams:
                    {
                        page: this.page,
                        sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
                    },
        });
        this.loadAll();
    }
}
