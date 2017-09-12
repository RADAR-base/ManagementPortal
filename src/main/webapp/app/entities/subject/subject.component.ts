import {
    Component, OnInit, OnDestroy, Input, OnChanges, SimpleChanges,
    SimpleChange
} from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';
import {Project} from "../project/project.model";

@Component({
    selector: 'jhi-subject',
    templateUrl: './subject.component.html'
})
export class SubjectComponent implements OnInit, OnDestroy , OnChanges{

    @Input() project : Project;
    subjects: Subject[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private subjectService: SubjectService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['subject' , 'project' , 'projectStatus']);
    }

    loadAll() {
        this.subjectService.query().subscribe(
            (res: Response) => {
                this.subjects = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    ngOnInit() {
        if(this.project) {
            this.loadAllFromProject();
        }
        else {
            this.loadAll();
        }
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInSubjects();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Subject) {
        return item.id;
    }
    registerChangeInSubjects() {
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    ngOnChanges(changes: SimpleChanges) {
        const project: SimpleChange = changes.project;
        this.project = project.currentValue;
        this.loadAllFromProject();
    }

    private loadAllFromProject() {
        this.subjectService.findAllByProject({
            projectId: this.project.id}).subscribe(
            (res: Response) => {
                this.subjects = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
}
