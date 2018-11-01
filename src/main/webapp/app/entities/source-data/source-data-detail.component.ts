import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs/Rx';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';

@Component({
    selector: 'jhi-source-data-detail',
    templateUrl: './source-data-detail.component.html',
})
export class SourceDataDetailComponent implements OnInit, OnDestroy {

    sourceData: SourceData;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
            private eventManager: EventManager,
            private jhiLanguageService: JhiLanguageService,
            private sourceDataService: SourceDataService,
            private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.setLocations(['sourceData', 'processingState']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['sourceDataName']);
        });
        this.registerChangeInSourceData();
    }

    load(sourceDataType) {
        this.sourceDataService.find(sourceDataType).subscribe((sourceData) => {
            this.sourceData = sourceData;
        });
    }

    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInSourceData() {
        this.eventSubscriber = this.eventManager.subscribe('sourceDataListModification', (response) => this.load(this.sourceData.sourceDataType));
    }
}
