/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { HttpServerRequests } from 'app/admin/metrics/metrics.model';
import { filterNaN } from 'app/core/util/operators';

@Component({
  selector: 'jhi-metrics-request',
  templateUrl: './metrics-request.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MetricsRequestComponent {
  /**
   * object containing http request related metrics
   */
  @Input() requestMetrics?: HttpServerRequests;

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  @Input() updating?: boolean;

  filterNaN = (input: number): number => filterNaN(input);
}
