/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {Component, Input} from '@angular/core';

import {GarbageCollector} from 'app/admin/metrics/metrics.model';

@Component({
    selector: 'jhi-metrics-garbagecollector',
    templateUrl: './metrics-garbagecollector.component.html',
})
export class MetricsGarbageCollectorComponent {
    /**
     * object containing garbage collector related metrics
     */
    @Input() garbageCollectorMetrics?: GarbageCollector;

    /**
     * boolean field saying if the metrics are in the process of being updated
     */
    @Input() updating?: boolean;
}
