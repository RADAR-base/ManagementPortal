/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {Component, Input} from '@angular/core';

import {JvmMetrics} from '../../metrics.model';

@Component({
    selector: 'jhi-jvm-memory',
    templateUrl: './jvm-memory.component.html',
})
export class JvmMemoryComponent {
    /**
     * object containing all jvm memory metrics
     */
    @Input() jvmMemoryMetrics?: { [key: string]: JvmMetrics };

    /**
     * boolean field saying if the metrics are in the process of being updated
     */
    @Input() updating?: boolean;
}
