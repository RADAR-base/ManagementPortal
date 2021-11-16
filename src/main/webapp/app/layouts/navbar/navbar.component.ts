import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';

import { DEBUG_INFO_ENABLED, VERSION } from '../../app.constants';
import {
    JhiLanguageHelper,
    LoginModalService,
    LoginService,
    Principal,
    Project,
    ProjectService,
} from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

import { ProfileService } from '../profiles/profile.service';

@Component({
    selector: 'jhi-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: [
        'navbar.scss',
    ],
})
export class NavbarComponent implements OnInit, OnDestroy {

    inProduction: boolean;
    isNavbarCollapsed: boolean;
    languages: any[];
    apiDocsEnabled: boolean;
    modalRef: NgbModalRef;
    version: string;

    projects: Project[];
    private subscriptions: Subscription;

    constructor(
            private loginService: LoginService,
            private languageHelper: JhiLanguageHelper,
            public principal: Principal,
            private loginModalService: LoginModalService,
            private profileService: ProfileService,
            private router: Router,
            private eventManager: EventManager,
            private translateService: TranslateService,
            private userService: UserService,
    ) {
        this.version = DEBUG_INFO_ENABLED ? 'v' + VERSION : '';
        this.isNavbarCollapsed = true;
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.loadRelevantProjects();
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });

        this.profileService.getProfileInfo().then((profileInfo) => {
            this.inProduction = profileInfo.inProduction;
            this.apiDocsEnabled = profileInfo.apiDocsEnabled;
        });
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    loadRelevantProjects() {
        this.subscriptions.add(this.principal.getAuthenticationState()
            .pipe(
              tap(account => this.currentAccount = account),
              switchMap(account => {
                  if (account) {
                      return this.userService.findProject(account.login);
                  } else {
                      return of([]);
                  }
              })
            )
            .subscribe(projects => this.projects = projects));
    }

    trackProjectName(index: number, item: Project) {
        return item.projectName;
    }

    changeLanguage(languageKey: string) {
        this.translateService.use(languageKey);
    }

    collapseNavbar() {
        this.isNavbarCollapsed = true;
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
}
