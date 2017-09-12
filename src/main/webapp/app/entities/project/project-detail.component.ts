import {
    Component, OnInit, OnDestroy, ComponentFactoryResolver, ViewChild,
    OnChanges
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { Project } from './project.model';
import { ProjectService } from './project.service';
import {Source} from "../source/source.model";
import {SourceComponent} from "../source/source.component";

@Component({
    selector: 'jhi-project-detail',
    templateUrl: './project-detail.component.html',
    styleUrls: ['project-detail.component.scss'],
})
export class ProjectDetailComponent implements OnInit, OnDestroy , OnChanges {

    project: Project;
    private subscription: any;
    private eventSubscriber: Subscription;

    sources: Source[];

    @ViewChild(SourceComponent)
    private sourceComponent: SourceComponent;


    showSources : boolean;
    showSubjects : boolean;
    showDeviceTypes : boolean;
    showProjectAdmins : boolean;
    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private projectService: ProjectService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['project', 'projectStatus' , 'source']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInProjects();
        this.viewSources();
        // this.sourceComponent.ngOnInit();
    }

    load(id) {
        this.projectService.find(id).subscribe((project) => {
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
        this.eventSubscriber = this.eventManager.subscribe('projectListModification', (response) => this.load(this.project.id));
    }

    viewSources() {
        this.showSources = true;
        this.showSubjects = false;
        this.showDeviceTypes = false;
        this.showProjectAdmins = false;
    }

    viewSubjects() {
        this.showSources = false;
        this.showSubjects = true;
        this.showDeviceTypes = false;
        this.showProjectAdmins = false;
    }
    ngOnChanges() {
        console.log('parent changed');
    }
}
