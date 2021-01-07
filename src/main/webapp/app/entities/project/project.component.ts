import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { AlertService, EventManager, JhiLanguageService, ParseLinks } from 'ng-jhipster';
import { ITEMS_PER_PAGE, Project, ProjectService } from '../../shared';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import { PagingParams } from '../../shared/commons';

@Component({
    selector: 'jhi-project',
    templateUrl: './project.component.html',
})
export class ProjectComponent implements OnInit, OnDestroy {
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

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private projectService: ProjectService,
            private alertService: AlertService,
            private eventManager: EventManager,
            private parseLinks: ParseLinks,
            private activatedRoute: ActivatedRoute,
            private router: Router,
    ) {
        this.projects = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        const pagingParams$ = this.activatedRoute.data.map<any, PagingParams>(data => {
            const fallback = { page: 1, predicate: 'id', ascending: true };
            return data['pagingParams'] || fallback;
        });
        this.routeData = pagingParams$.subscribe(params => {
            this.page = params.page;
            this.previousPage = params.page;
            this.ascending = params.ascending;
            this.predicate = params.predicate;
        });
        this.jhiLanguageService.setLocations(['project', 'projectStatus']);
    }

    loadAll() {
        this.projectService.query(
                {
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    sort: this.sort(),
                },
        ).subscribe(
                (res: HttpResponse<Project[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpErrorResponse) => this.onError(res.message),
        );
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInProjects();
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
        this.links = this.parseLinks.parse(headers.get('link'));
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
