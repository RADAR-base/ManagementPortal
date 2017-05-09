import { Component, OnInit, OnDestroy } from '@angular/core';
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { DeviceType } from './device-type.model';
import { DeviceTypeService } from './device-type.service';
import { ITEMS_PER_PAGE, Principal } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';

@Component({
    selector: 'jhi-device-type',
    templateUrl: './device-type.component.html'
})
export class DeviceTypeComponent implements OnInit, OnDestroy {
deviceTypes: DeviceType[];
    currentAccount: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private deviceTypeService: DeviceTypeService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['deviceType']);
    }

    loadAll() {
        this.deviceTypeService.query().subscribe(
            (res: Response) => {
                this.deviceTypes = res.json();
            },
            (res: Response) => this.onError(res.json())
        );
    }
    ngOnInit() {
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInDeviceTypes();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: DeviceType) {
        return item.id;
    }
    registerChangeInDeviceTypes() {
        this.eventSubscriber = this.eventManager.subscribe('deviceTypeListModification', (response) => this.loadAll());
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
