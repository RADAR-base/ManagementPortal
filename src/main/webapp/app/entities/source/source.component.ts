import { Component, OnInit, OnDestroy } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Source } from './source.model';
import { SourceService } from './source.service';
import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';

@Component({
    selector: 'jhi-source',
    templateUrl: './source.component.html'
})
export class SourceComponent implements OnInit, OnDestroy {
sources: Source[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private sourceService: SourceService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['source']);
    }

    loadAll() {
        this.sourceService.query().subscribe(
            (res: Response) => {
                this.sources = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    ngOnInit() {
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInDevices();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: Source) {
        return item.id;
    }
    registerChangeInDevices() {
        this.eventSubscriber = this.eventManager.subscribe('sourceListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
