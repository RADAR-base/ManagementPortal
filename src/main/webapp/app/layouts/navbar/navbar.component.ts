import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

import {
    JhiLanguageHelper,
    LoginModalService,
    LoginService,
    Principal,
    Project,
    ProjectService,
    Organization,
    OrganizationService
} from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

import { ProfileService } from '../profiles/profile.service';
import {environment} from "../../../environments/environment";

@Component({
    selector: 'jhi-navbar',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './navbar.component.html',
    styleUrls: [
        'navbar.scss',
    ],
})
export class NavbarComponent {
    isNavbarCollapsed: boolean;
    modalRef: NgbModalRef;

    projects: Project[];
    organizations: Organization[];

    constructor(
        private loginService: LoginService,
        public languageHelper: JhiLanguageHelper,
        public principal: Principal,
        private loginModalService: LoginModalService,
        public profileService: ProfileService,
        private router: Router,
        private eventManager: EventManager,
        private translateService: TranslateService,
        public projectService: ProjectService,
        public organizationService: OrganizationService,
    ) {
        this.isNavbarCollapsed = true;
    }

    trackProjectName(index: number, item: Project) {
        return item.projectName;
    }

    trackOrganizationName(index: number, item: Organization) {
        return item.name;
    }

    changeLanguage(languageKey: string) {
        this.translateService.use(languageKey);
    }

    collapseNavbar() {
        this.isNavbarCollapsed = true;
    }

    login() {
        window.location.href =  environment.KRATOS_URL + `/login?return_to=${window.location.href.split('?')[0]}`;
    }

    logout() {
        this.collapseNavbar();
        this.loginService.logout();
        this.router.navigate(['']);
    }

    toggleNavbar() {
        this.isNavbarCollapsed = !this.isNavbarCollapsed;
    }

    redirectToProfile() {
        window.location.href =  environment.KRATOS_URL + '/settings';
    }
}
