import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ITEMS_PER_PAGE} from '..';
import {EntityRevision} from '../../entities/revision/entity-revision.model';
import {SubjectService} from './subject.service';
import {HttpResponse} from '@angular/common/http';
import {parseLinks} from '../util/parse-links-util';

@Component({
    selector: 'jhi-subject-revision-list',
    templateUrl: './subject-revision-list.component.html',
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
        private subjectService: SubjectService,
        private route: ActivatedRoute,
        private router: Router,
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
            sort: this.sort(),
        }).subscribe((response: HttpResponse<any>) => {
            this.links = parseLinks(response.headers.get('link'));
            this.totalItems = parseInt(response.headers.get('X-Total-Count'), 10);
            this.queryCount = this.totalItems;
            this.revisions = response.body;
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
                    sort: this.sort(),
                },
        });
        this.load(this.subjectLogin);
    }

    sort() {
        return [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
