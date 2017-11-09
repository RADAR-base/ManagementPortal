import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager , JhiLanguageService  } from 'ng-jhipster';

import { SensorData } from './sensor-data.model';
import { SensorDataService } from './sensor-data.service';

@Component({
    selector: 'jhi-sensor-data-detail',
    templateUrl: './sensor-data-detail.component.html'
})
export class SensorDataDetailComponent implements OnInit, OnDestroy {

    sensorData: SensorData;
    private subscription: any;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: EventManager,
        private jhiLanguageService: JhiLanguageService,
        private sensorDataService: SensorDataService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['sensorData', 'dataType']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['sensorName']);
        });
        this.registerChangeInSensorData();
    }

    load(sensorName) {
        this.sensorDataService.find(sensorName).subscribe((sensorData) => {
            this.sensorData = sensorData;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInSensorData() {
        this.eventSubscriber = this.eventManager.subscribe('sensorDataListModification', (response) => this.load(this.sensorData.sensorName));
    }
}
