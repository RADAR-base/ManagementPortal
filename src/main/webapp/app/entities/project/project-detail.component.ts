import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';
import { Source } from '../../shared/source';

import { Project } from './project.model';
import { ProjectService } from './project.service';

@Component({
    selector: 'jhi-project-detail',
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy {

    project: Project;
    private subscription: any;
    private eventSubscriber: Subscription;

    sources: Source[];

    showSources: boolean;
    showSubjects: boolean;
    showSourceTypes: boolean;
    showProjectAdmins: boolean;
    showProjectAnalysts: boolean;

    constructor(
            private eventManager: EventManager,
            private jhiLanguageService: JhiLanguageService,
            private projectService: ProjectService,
            private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.setLocations(['project', 'projectStatus']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['projectName']);
        });
        this.registerChangeInProjects();
        this.viewSubjects();
        // this.sourceComponent.ngOnInit();
    }

    load(projectName) {
        this.projectService.find(projectName).subscribe((project) => {
            this.project = project;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInProjects() {
        this.eventSubscriber = this.eventManager.subscribe('projectListModification',
                () => this.load(this.project.projectName));
    }

    viewSources() {
        this.showSources = true;
        this.showSubjects = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = false;
    }

    viewSubjects() {
        this.showSources = false;
        this.showSubjects = true;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = false;
    }

    viewProjectAdmins() {
        this.showSources = false;
        this.showSubjects = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = true;
        this.showProjectAnalysts = false;
    }

    viewProjectAnalysts() {
        this.showSources = false;
        this.showSubjects = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = true;
    }
}
