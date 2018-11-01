import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import { Account, LoginModalService, Principal, Project, UserService } from '../shared';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss',
    ],

})
export class HomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;
    projects: Project[];

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private principal: Principal,
            private loginModalService: LoginModalService,
            private eventManager: EventManager,
            private userService: UserService,
    ) {
        this.jhiLanguageService.setLocations(['home']);
    }

    ngOnInit() {
        this.loadRelevantProjects();
        this.registerAuthenticationSuccess();
    }

    loadRelevantProjects() {
        this.principal.identity().then((account) => {
            this.account = account;
            if (this.account) {
                this.userService.findProject(this.account.login).subscribe(res => {
                    this.projects = res.json();
                });
            }
        });
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', (message) => {
            this.principal.identity().then((account) => {
                this.account = account;
                this.loadRelevantProjects();
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
