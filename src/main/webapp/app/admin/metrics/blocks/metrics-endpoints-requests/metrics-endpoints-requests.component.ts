/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { Component, Input } from '@angular/core';

import { Services } from 'app/admin/metrics/metrics.model';

@Component({
  selector: 'jhi-metrics-endpoints-requests',
  templateUrl: './metrics-endpoints-requests.component.html',
})
export class MetricsEndpointsRequestsComponent {
  /**
   * object containing service related metrics
   */
  @Input() endpointsRequestsMetrics?: Services;

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  @Input() updating?: boolean;
}
