import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';

import { Account, LoginModalService, Principal, Project, UserService } from '../shared';
import { EventManager } from '../shared/util/event-manager.service';

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

    private loadRelevantProjects() {
        this.principal.identity().then((account) => {
            this.account = account;
            if (this.account) {
                this.userService.findProject(this.account.login)
                        .subscribe(res => this.projects = res);
            }
        });
    }

    private registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', () => {
            this.principal.identity().then((account) => {
                this.account = account;
                this.loadRelevantProjects();
            });
        });
    }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
