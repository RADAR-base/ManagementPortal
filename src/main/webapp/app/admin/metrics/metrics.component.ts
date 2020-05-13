import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';

import { JhiMetricsMonitoringModalComponent } from './metrics-modal.component';
import { JhiMetricsService } from './metrics.service';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'jhi-metrics',
    templateUrl: './metrics.component.html',
})
export class JhiMetricsMonitoringComponent implements OnInit {
    metrics: any = {};
    cachesStats: any = {};
    servicesStats: any = {};
    updatingMetrics = true;
    JCACHE_KEY: string;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private modalService: NgbModal,
            private metricsService: JhiMetricsService,
    ) {
        this.JCACHE_KEY = 'jcache.statistics';
        this.jhiLanguageService.setLocations(['metrics']);
    }

    ngOnInit() {
        this.refresh();
    }

    refresh() {
        this.updatingMetrics = true;
        this.metricsService.getMetrics().subscribe((response: HttpResponse<any>) => {
            this.metrics = response.body;
            this.updatingMetrics = false;
            this.servicesStats = {};
            this.cachesStats = {};
            Object.keys(this.metrics.timers).forEach((key) => {
                const value = this.metrics.timers[key];
                if (key.indexOf('web.rest') !== -1 || key.indexOf('service') !== -1) {
                    this.servicesStats[key] = value;
                }
            });
            Object.keys(this.metrics.gauges).forEach((key) => {
                if (key.indexOf('jcache.statistics') !== -1) {
                    const value = this.metrics.gauges[key].value;
                    // remove gets or puts
                    const index = key.lastIndexOf('.');
                    const newKey = key.substr(0, index);

                    // Keep the name of the domain
                    this.cachesStats[newKey] = {
                        'name': this.JCACHE_KEY.length,
                        'value': value,
                    };
                }
            });
        });
    }

    refreshThreadDumpData() {
        this.metricsService.threadDump().subscribe((data) => {
            const modalRef = this.modalService.open(JhiMetricsMonitoringModalComponent, {size: 'lg'});
            modalRef.componentInstance.threadDump = data;
            modalRef.result.then((result) => {
                // Left blank intentionally, nothing to do here
            }, (reason) => {
                // Left blank intentionally, nothing to do here
            });
        });
    }

}
