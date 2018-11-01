import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';

import { Subject } from './subject.model';
import { SubjectService } from './subject.service';

@Component({
    selector: 'jhi-subject-detail',
    templateUrl: './subject-detail.component.html',
})
export class SubjectDetailComponent implements OnInit, OnDestroy {

    subject: Subject;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
            private eventManager: EventManager,
            private jhiLanguageService: JhiLanguageService,
            private subjectService: SubjectService,
            private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.addLocation('subject');
        this.jhiLanguageService.addLocation('audits');
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['login']);
        });
        this.registerChangeInSubjects();
    }

    load(id) {
        this.subjectService.find(id).subscribe((subject) => {
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
        this.eventSubscriber = this.eventManager.subscribe('subjectListModification', (response) => this.load(this.subject.login));
    }
}
