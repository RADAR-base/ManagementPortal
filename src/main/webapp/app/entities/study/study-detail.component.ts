import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { Study } from './study.model';
import { StudyService } from './study.service';

@Component({
    selector: 'jhi-study-detail',
    templateUrl: './study-detail.component.html'
})
export class StudyDetailComponent implements OnInit, OnDestroy {

    study: Study;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private studyService: StudyService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['study', 'studyStatus']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInStudies();
    }

    load(id) {
        this.studyService.find(id).subscribe((study) => {
            this.study = study;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInStudies() {
        this.eventSubscriber = this.eventManager.subscribe('studyListModification', (response) => this.load(this.study.id));
    }
}
