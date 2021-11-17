import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Source } from '../../shared/source/source.model';
import { Project, ProjectService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';

@Component({
    selector: 'jhi-project-detail',
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy {

    project: Project;
    private subscription = new Subscription();
    private eventSubscriber: Subscription;

    sources: Source[];

    showSources: boolean;
    showSubjects: boolean;
    showProjectGroups: boolean;
    showSourceTypes: boolean;
    showProjectAdmins: boolean;
    showProjectAnalysts: boolean;

    constructor(
            private eventManager: EventManager,
            private projectService: ProjectService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscription.add(this.route.params.subscribe((params) => {
            this.load(params['projectName']);
        }));
        this.registerChangeInProjects();
        this.viewSubjects();
    }

    load(projectName) {
        this.subscription.add(this.projectService.find(projectName).subscribe((project) => {
            this.project = project;
        }));
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
        this.showProjectGroups = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = false;
    }

    viewSubjects() {
        this.showSources = false;
        this.showSubjects = true;
        this.showProjectGroups = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = false;
    }

    viewProjectGroups() {
        this.showSources = false;
        this.showSubjects = false;
        this.showProjectGroups = true;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = false;
    }

    viewProjectAdmins() {
        this.showSources = false;
        this.showSubjects = false;
        this.showProjectGroups = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = true;
        this.showProjectAnalysts = false;
    }

    viewProjectAnalysts() {
        this.showSources = false;
        this.showSubjects = false;
        this.showProjectGroups = false;
        this.showSourceTypes = false;
        this.showProjectAdmins = false;
        this.showProjectAnalysts = true;
    }
}
