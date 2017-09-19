import {
    Component, OnInit, OnDestroy, Input, SimpleChanges, SimpleChange,
    OnChanges
} from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManager, PaginationUtil, ParseLinks, AlertService, JhiLanguageService } from 'ng-jhipster';

import { ITEMS_PER_PAGE, Principal, User, UserService } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';
import {Project} from "../../entities/project/project.model";

@Component({
    selector: 'common-user-mgmt',
    templateUrl: './common-user-management.component.html'
})
export class CommonUserMgmtComponent implements OnInit, OnChanges{

    currentAccount: any;
    users: User[];
    error: any;
    success: any;
    links: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    @Input() project : Project;
    @Input() authority : String;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private userService: UserService,
        private parseLinks: ParseLinks,
        private alertService: AlertService,
        private eventManager: EventManager,
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.jhiLanguageService.setLocations(['user-management' , 'project' , 'projectStatus']);
    }

    ngOnInit() {
        this.loadAll();
        this.registerChangeInUsers();
    }


    registerChangeInUsers() {
        this.eventManager.subscribe('userListModification', (response) => this.loadAll());
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project? changes.project: null;
        if(project){
            this.project = project.currentValue;
            this.loadAll();
        }
    }

    loadAll() {
       if(this.project && this.authority) {
           this.userService.findByProjectAndAuthority(
               {
                   projectId: this.project.id ,
                   authority: this.authority,
               }
               ).subscribe(
               (res: Response) => this.users = res.json());
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

    private onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.users = data;
    }

    private onError(error) {
        this.alertService.error(error.error, error.message, null);
    }
}
