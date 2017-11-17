import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { DeviceType } from './device-type.model';
import { DeviceTypeService } from './device-type.service';

@Component({
    selector: 'jhi-device-type-detail',
    templateUrl: './device-type-detail.component.html'
})
export class DeviceTypeDetailComponent implements OnInit, OnDestroy {

    deviceType: DeviceType;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private deviceTypeService: DeviceTypeService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['deviceType', 'sourceTypeScope']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['deviceTypeProducer'], params['deviceTypeModel'], params['catalogVersion']);
        });
        this.registerChangeInDeviceTypes();
    }

    load(producer: string, model: string, version: string) {
        this.deviceTypeService.find(producer, model, version).subscribe((deviceType) => {
            this.deviceType = deviceType;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInDeviceTypes() {
        this.eventSubscriber = this.eventManager.subscribe('deviceTypeListModification',
            (response) => this.load(this.deviceType.deviceProducer, this.deviceType.deviceModel, this.deviceType.catalogVersion));
    }
}
