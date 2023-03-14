/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from "@angular/core";

@Component({
    selector: 'app-filter-badge',
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <div class="badge badge-pill badge-primary mx-1" *ngIf="value"
             (click)="clear.emit()">
            <span>{{ text | translate }}: {{value}}</span>
            <span aria-hidden="true"> &times;</span>
        </div>
    `
})
export class FilterBadgeComponent {
  @Input()
  text: string
  @Input()
  value: string
  @Output()
  clear: EventEmitter<void> = new EventEmitter()
}
