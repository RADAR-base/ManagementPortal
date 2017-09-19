import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { Source } from './source.model';
import { SourceService } from './source.service';

@Component({
    selector: 'jhi-source-detail',
    templateUrl: './source-detail.component.html'
})
export class SourceDetailComponent implements OnInit, OnDestroy {

    source: Source;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private sourceService: SourceService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['source']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInDevices();
    }

    load(id) {
        this.sourceService.find(id).subscribe((source) => {
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
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification', (response) => this.load(this.source.id));
    }
}
