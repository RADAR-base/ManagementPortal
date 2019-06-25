import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';

import { Source } from './source.model';
import { SourceService } from './source.service';

@Component({
    selector: 'jhi-source-detail',
    templateUrl: './source-detail.component.html',
})
export class SourceDetailComponent implements OnInit, OnDestroy {

    source: Source;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
            private eventManager: EventManager,
            private jhiLanguageService: JhiLanguageService,
            private sourceService: SourceService,
            private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.setLocations(['source']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['sourceName']);
        });
        this.registerChangeInDevices();
    }

    load(sourceName) {
        this.sourceService.find(sourceName).subscribe((source) => {
            this.source = source;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInDevices() {
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification',
                (response) => this.load(this.source.sourceName));
    }
}
