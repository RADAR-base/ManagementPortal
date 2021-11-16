import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { of, Subscription } from 'rxjs';

import { DEBUG_INFO_ENABLED, VERSION } from '../../app.constants';
import {
  JhiLanguageHelper,
  LoginModalService,
  LoginService,
  Principal,
  Project,
  ProjectService,
  UserService
} from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

import { ProfileService } from '../profiles/profile.service';
import { switchMap, tap } from "rxjs/operators";

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
    apiDocsEnabled: boolean;
    modalRef: NgbModalRef;
    version: string;

    projects: Project[];
    currentAccount: any;
    private subscriptions: Subscription;

    constructor(
      private loginService: LoginService,
      public languageHelper: JhiLanguageHelper,
      private principal: Principal,
      private loginModalService: LoginModalService,
      private profileService: ProfileService,
      private router: Router,
      private eventManager: EventManager,
      private translateService: TranslateService,
      public projectService: ProjectService,
    ) {
        this.version = DEBUG_INFO_ENABLED ? 'v' + VERSION : '';
        this.isNavbarCollapsed = true;
        this.subscriptions = new Subscription();
    }

    ngOnInit() {
        this.principal.identity().then(account => this.currentAccount = account);
        this.profileService.getProfileInfo().then((profileInfo) => {
            this.inProduction = profileInfo.inProduction;
            this.apiDocsEnabled = profileInfo.apiDocsEnabled;
        });
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
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
}
