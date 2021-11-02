/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { ProcessMetrics } from 'app/admin/metrics/metrics.model';

@Component({
  selector: 'jhi-metrics-system',
  templateUrl: './metrics-system.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MetricsSystemComponent {
  /**
   * object containing thread related metrics
   */
  @Input() systemMetrics?: ProcessMetrics;

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  @Input() updating?: boolean;

  convertMillisecondsToDuration(ms: number): string {
    const times = {
      year: 31557600000,
      month: 2629746000,
      day: 86400000,
      hour: 3600000,
      minute: 60000,
      second: 1000,
    };
    let timeString = '';
    for (const [key, value] of Object.entries(times)) {
      if (Math.floor(ms / value) > 0) {
        let plural = '';
        if (Math.floor(ms / value) > 1) {
          plural = 's';
        }
        timeString += `${Math.floor(ms / value).toString()} ${key.toString()}${plural} `;
        ms = ms - value * Math.floor(ms / value);
      }
    }
    return timeString;
  }
}
