import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Subscription';

import { DEBUG_INFO_ENABLED, VERSION } from '../../app.constants';
import { JhiLanguageHelper, LoginModalService, LoginService, Principal, Project, UserService } from '../../shared';

import { ProfileService } from '../profiles/profile.service'; // FIXME barrel doesn't work here

@Component({
    selector: 'jhi-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: [
        'navbar.scss',
    ],
})
export class NavbarComponent implements OnInit {

    inProduction: boolean;
    isNavbarCollapsed: boolean;
    languages: any[];
    swaggerEnabled: boolean;
    modalRef: NgbModalRef;
    version: string;
    eventSubscriber: Subscription;

    projects: Project[];
    currentAccount: any;

    constructor(
            private loginService: LoginService,
            private languageHelper: JhiLanguageHelper,
            private languageService: JhiLanguageService,
            private principal: Principal,
            private loginModalService: LoginModalService,
            private profileService: ProfileService,
            private router: Router,
            private eventManager: EventManager,
            private userService: UserService,
    ) {
        this.version = DEBUG_INFO_ENABLED ? 'v' + VERSION : '';
        this.isNavbarCollapsed = true;
        this.languageService.addLocation('home');
    }

    ngOnInit() {
        this.registerChangeInAuthentication();
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });

        this.profileService.getProfileInfo().subscribe((profileInfo) => {
            this.inProduction = profileInfo.inProduction;
            this.swaggerEnabled = profileInfo.swaggerEnabled;
        });
        this.loadRelevantProjects();
        this.registerChangeInUsers();
    }

    registerChangeInAuthentication() {
        this.eventManager.subscribe('authenticationSuccess', (message) => {
            this.loadRelevantProjects();
        });
    }

    loadRelevantProjects() {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
            if (this.currentAccount) {
                this.userService.findProject(this.currentAccount.login).subscribe(res => {
                    this.projects = res.json();
                });
            }
        });
    }

    registerChangeInUsers() {
        this.eventSubscriber = this.eventManager.subscribe('userListModification', (response) => this.loadRelevantProjects());
    }

    trackProjectName(index: number, item: Project) {
        return item.projectName;
    }

    changeLanguage(languageKey: string) {
        this.languageService.changeLanguage(languageKey);
    }

    collapseNavbar() {
        this.isNavbarCollapsed = true;
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    logout() {
        this.collapseNavbar();
        this.loginService.logout();
        this.router.navigate(['']);
    }

    toggleNavbar() {
        this.isNavbarCollapsed = !this.isNavbarCollapsed;
    }

    getImageUrl() {
        return this.isAuthenticated() ? this.principal.getImageUrl() : null;
    }
}
