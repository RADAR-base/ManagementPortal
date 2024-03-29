import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';

import { ITEMS_PER_PAGE } from '../../shared';

import { Audit } from './audit.model';
import { AuditsService } from './audits.service';
import { HttpResponse } from '@angular/common/http';
import { parseLinks } from '../../shared/util/parse-links-util';
import { Subscription } from 'rxjs';

@Component({
    selector: 'jhi-audit',
    templateUrl: './audits.component.html',
})
export class AuditsComponent implements OnInit, OnDestroy {
    audits: Audit[];
    fromDate: string;
    itemsPerPage: any;
    page: number;
    orderProp: string;
    reverse: boolean;
    toDate: string;
    totalItems: number;
    datePipe: DatePipe;

    private subscriptions: Subscription = new Subscription();

    constructor(
            private auditsService: AuditsService,
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.page = 1;
        this.reverse = false;
        this.orderProp = 'timestamp';
        this.datePipe = new DatePipe('en');
    }

    getAudits() {
        return this.sortAudits(this.audits);
    }

    loadPage(page: number) {
        this.page = page;
        this.onChangeDate();
    }

    ngOnInit() {
        this.today();
        this.previousMonth();
        this.onChangeDate();
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    onChangeDate() {
        this.subscriptions.add(this.auditsService.query({
            page: this.page - 1,
            size: this.itemsPerPage,
            fromDate: this.fromDate,
            toDate: this.toDate,
        }).subscribe((res: HttpResponse<any>) => {
            this.audits = res.body;
            this.totalItems = +res.headers.get('X-Total-Count');
        }));
    }

    previousMonth() {
        const dateFormat = 'yyyy-MM-dd';
        let fromDate: Date = new Date();

        if (fromDate.getMonth() === 0) {
            fromDate = new Date(fromDate.getFullYear() - 1, 11, fromDate.getDate());
        } else {
            fromDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
        }

        this.fromDate = this.datePipe.transform(fromDate, dateFormat);
    }

    today() {
        const dateFormat = 'yyyy-MM-dd';
        // Today + 1 day - needed if the current day must be included
        const today: Date = new Date();
        today.setDate(today.getDate() + 1);
        const date = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        this.toDate = this.datePipe.transform(date, dateFormat);
    }

    private sortAudits(audits: Audit[]) {
        audits = audits.slice(0).sort((a, b) => {
            if (a[this.orderProp] < b[this.orderProp]) {
                return -1;
            } else if ([b[this.orderProp] < a[this.orderProp]]) {
                return 1;
            } else {
                return 0;
            }
        });

        return this.reverse ? audits.reverse() : audits;
    }
}
