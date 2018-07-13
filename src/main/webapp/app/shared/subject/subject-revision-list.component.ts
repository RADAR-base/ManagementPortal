import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService, ParseLinks  } from 'ng-jhipster';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { EntityRevision } from '../../entities/revision/entity-revision.model';
import { ITEMS_PER_PAGE } from '..';

@Component({
    selector: 'jhi-subject-revision-list',
    templateUrl: './subject-revision-list.component.html'
})
export class SubjectRevisionListComponent implements OnInit, OnDestroy {

    revisions: EntityRevision[];
    subjectLogin: string;
    itemsPerPage: number;
    links: any;
    page: any;
    predicate: any;
    queryCount: any;
    reverse: any;
    totalItems: number;
    routeData: any;
    previousPage: any;
    private subscription: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private subjectService: SubjectService,
        private route: ActivatedRoute,
        private parseLinks: ParseLinks,
        private router: Router
    ) {
        this.revisions = [];
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.route.data.subscribe((data) => {
            if (data['pagingParams']) {
                this.page = data['pagingParams'].page;
                this.previousPage = data['pagingParams'].page;
                this.reverse = data['pagingParams'].ascending;
                this.predicate = data['pagingParams'].predicate;
            } else {
                this.page = 1;
                this.previousPage = 1;
                this.predicate = 'user.login';
                this.reverse = true;
            }
        });
        this.jhiLanguageService.setLocations(['subject', 'audits']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.subjectLogin = params['login'];
            this.load(params['login']);
        });
    }

    trackRevision(index: number, rev: EntityRevision) {
        return rev.id;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }

    load(id) {
        this.subjectService.findRevisions(id, {
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort()
            }).subscribe((response) => {
                this.links = this.parseLinks.parse(response.headers.get('link'));
                this.totalItems = parseInt(response.headers.get('X-Total-Count'));
                this.queryCount = this.totalItems;
                this.revisions = response.json();
            });
    }

    loadPage(page) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/subject', this.subjectLogin, 'revisions'], {
            queryParams:
                {
                    page: this.page,
                    size: this.itemsPerPage,
                    sort: this.sort()
                }
        });
        this.load(this.subjectLogin);
    }
    
    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        return result;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
