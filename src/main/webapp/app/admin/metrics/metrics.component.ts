import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {combineLatest} from 'rxjs';

import {JhiMetricsService} from './metrics.service';
import {Metrics, Thread} from './metrics.model';

@Component({
    selector: 'jhi-metrics',
    templateUrl: './metrics.component.html',
})
export class JhiMetricsMonitoringComponent implements OnInit {
    metrics?: Metrics;
    threads?: Thread[];
    updatingMetrics = true;

    constructor(private metricsService: JhiMetricsService, private changeDetector: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.refresh();
    }

    refresh(): void {
        this.updatingMetrics = true;
        combineLatest([this.metricsService.getMetrics(), this.metricsService.threadDump()]).subscribe(([metrics, threadDump]) => {
            this.metrics = metrics;
            this.threads = threadDump.threads;
            this.updatingMetrics = false;
            this.changeDetector.markForCheck();
        });
    }

    metricsKeyExists(key: keyof Metrics): boolean {
        return Boolean(this.metrics?.[key]);
    }

    metricsKeyExistsAndObjectNotEmpty(key: keyof Metrics): boolean {
        return Boolean(this.metrics?.[key] && JSON.stringify(this.metrics[key]) !== '{}');
    }
}
