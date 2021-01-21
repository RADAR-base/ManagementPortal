import { Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges } from '@angular/core';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { ITEMS_PER_PAGE, Project, User, UserService } from '..';

@Component({
    selector: 'jhi-common-user-mgmt',
    templateUrl: './common-user-management.component.html'
})
export class CommonUserMgmtComponent implements OnInit, OnChanges {
    users: User[];
    error: any;
    success: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    @Input() project: Project;
    @Input() authority: String;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private userService: UserService,
            private eventManager: EventManager,
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.jhiLanguageService.addLocation('user-management');
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInUsers();
    }

    registerChangeInUsers() {
        this.eventManager.subscribe('userListModification', () => this.loadAll());
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project ? changes.project : null;
        if (project) {
            this.project = project.currentValue;
            this.loadAll();
        }
    }

    loadAll() {
        if (this.project && this.authority) {
            this.userService.findByProjectAndAuthority(
                    {
                        projectName: this.project.projectName,
                        authority: this.authority,
                    },
            ).subscribe((res: any) => this.users = res);
        }
    }

    trackIdentity(index, item: User) {
        return item.id;
    }

    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.loadAll();
    }
}
