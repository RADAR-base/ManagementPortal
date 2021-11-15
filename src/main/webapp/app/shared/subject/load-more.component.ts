/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { Component, EventEmitter, Input, Output } from "@angular/core";

@Component({
  selector: 'app-load-more',
  template: `
      <div class="subject-pagination">
          <span *ngIf="totalItems && shownItems >= totalItems"
                class="all-loaded"
                [translate]="'managementPortalApp.subject.allLoaded'"
                [translateParams]="{ total: shownItems }"
          ></span>
          <a *ngIf="totalItems === null || totalItems === undefined || shownItems < totalItems"
             class="load-more"
             [ngClass]="totalItems ? 'load-more-limited' : 'load-more-unlimited'"
             (click)="load.next()"
             [translate]="totalItems ? 'managementPortalApp.subject.loadMore' : 'managementPortalApp.subject.loadMorePartial'"
             [translateParams]="{ shown: shownItems, total: totalItems }"
          ></a>
      </div>
  `,
  styleUrls: ['./load-more.component.scss']
})
export class LoadMoreComponent {
  @Input()
  totalItems?: number
  @Input()
  shownItems: number
  @Output()
  load: EventEmitter<void> = new EventEmitter()
}
