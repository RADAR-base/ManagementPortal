import {Component} from '@angular/core';
import {NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {LoginModalService, OrganizationService, Principal, Project, ProjectService,} from '../shared';
import {Subscription} from "rxjs";

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss',
    ],

})
export class HomeComponent {
    // implements OnInit, OnDestroy {
    modalRef: NgbModalRef;
    // projects: Project[];
    subscriptions: Subscription;
    private loginUrl = 'api/redirect/login';

    constructor(
        public principal: Principal,
        private loginModalService: LoginModalService,
        public projectService: ProjectService,
        public organizationService: OrganizationService,
    ) {
        this.subscriptions = new Subscription();
    }

    // ngOnInit() {
    //     this.loadRelevantProjects();
    // }
    //
    // ngOnDestroy() {
    //     this.subscriptions.unsubscribe();
    // }
    //
    // private loadRelevantProjects() {
    //     this.subscriptions.add(this.principal.account$
    //         .pipe(
    //           switchMap(account => {
    //             if (account) {
    //                 return this.userService.findProject(account.login);
    //             } else {
    //               return of([]);
    //             }
    //           })
    //         )
    //         .subscribe(projects => this.projects = projects));
    // }

    trackId(index: number, item: Project) {
        return item.projectName;
    }

    login() {
        window.location.href = this.loginUrl
    }
}
