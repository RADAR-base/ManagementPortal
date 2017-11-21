import { Component, OnInit, OnDestroy } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';
import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';

@Component({
    selector: 'jhi-source-data',
    templateUrl: './source-data.component.html'
})
export class SourceDataComponent implements OnInit, OnDestroy {
sourceData: SourceData[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceDataService: SourceDataService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['sourceData', 'processingState']);
    }

    loadAll() {
        this.sourceDataService.query().subscribe(
            (res: Response) => {
                this.sourceData = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    ngOnInit() {
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInSourceData();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: SourceData) {
        return item.id;
    }
    registerChangeInSourceData() {
        this.eventSubscriber = this.eventManager.subscribe('sourceDataListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
