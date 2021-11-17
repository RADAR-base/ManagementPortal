import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Source } from '../../shared/source/source.model';
import { Project, ProjectService } from '../../shared';
import { EventManager } from '../../shared/util/event-manager.service';
import { switchMap } from "rxjs/operators";

@Component({
    selector: 'jhi-project-detail',
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
    private subscription = new Subscription();
    project: Project;
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
        this.subscription.add(this.registerChangesInProjectName());
        this.viewSubjects();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private registerChangesInProjectName(): Subscription {
        return this.route.params.pipe(
            switchMap(({projectName}) => this.projectService.find(projectName)),
        ).subscribe(
            (project) => this.project = project,
        );
    }

    previousState() {
        window.history.back();
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
