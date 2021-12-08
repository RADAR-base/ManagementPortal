import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import { map } from 'rxjs/operators';

import {ITEMS_PER_PAGE, Organization, Project, ProjectService} from '../../shared';
import { PagingParams } from '../../shared/commons';
import { AlertService } from '../../shared/util/alert.service';
import { EventManager } from '../../shared/util/event-manager.service';
import { parseLinks } from '../../shared/util/parse-links-util';

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
    links: any;
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
        // this.projectService.query(
        //     {
        //         page: this.page - 1,
        //         size: this.itemsPerPage,
        //         sort: this.sort(),
        //         organizationName: this.organization.name
        //     },
        // ).subscribe(
        //     (res: HttpResponse<Project[]>) => this.onSuccess(res.body, res.headers),
        //     (res: HttpErrorResponse) => this.onError(res.message),
        // );
        this.projectService.findAllByOrganization(this.organization.name).subscribe({
            next: projects => this.projects = projects,
            error: error => this.alertService.error(error.message, null, null)
        })
        // this.projectService.query(
        //         {
        //             page: this.page - 1,
        //             size: this.itemsPerPage,
        //             sort: this.sort(),
        //             organizationId: this.organization.name
        //         },
        // ).subscribe(
        //         (res: HttpResponse<Project[]>) => this.onSuccess(res.body, res.headers),
        //         (res: HttpErrorResponse) => this.onError(res.message),
        // );
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
        this.eventSubscriber = this.eventManager.subscribe('projectListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    sort() {
        const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    private onSuccess(data, headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.projects = data;
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
