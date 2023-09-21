import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { EventManager } from '../util/event-manager.service';
import { Subject } from './subject.model';
import { SubjectService } from './subject.service';
import {HideableSubjectField, SiteSettingsService} from "./sitesettings.service";

@Component({
    selector: 'jhi-subject-detail',
    templateUrl: './subject-detail.component.html',
    styleUrls: ['./subject-detail.component.scss'],
})
export class SubjectDetailComponent implements OnInit, OnDestroy {

    subject: Subject;
    private subscription: any;
    private eventSubscriber: Subscription;
    public siteSettings$ =this.siteSettingsService.siteSettings$;

    constructor(
            private eventManager: EventManager,
            private subjectService: SubjectService,
            private siteSettingsService: SiteSettingsService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['login']);
        });
        this.registerChangeInSubjects();
    }

    load(id) {
        this.subjectService.find(id).subscribe((subject: Subject) => {
            this.subject = subject;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInSubjects() {
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', ({content}) => {
            if (content.subject.login === this.subject.login && (content.op === 'UPDATE' || content.op === 'CREATE')) {
                this.subject = content.subject;
            }
        });
    }

    protected readonly HideableSubjectField = HideableSubjectField;
}
