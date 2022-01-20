// Borrowed from ng-jhipster

import { Directive, ElementRef, EventEmitter, Input, Output } from '@angular/core';
import { SortOrder } from "./sort-util";

@Directive({
    selector: '[jhiSortOrder]'
})
export class JhiSortOrderDirective {
    @Input() order: SortOrder;

    sortIcon = 'fa-sort';
    sortAscIcon = 'fa-sort-asc';
    sortDescIcon = 'fa-sort-desc';
    sortIconSelector = 'span.fa';

    @Output() orderChange: EventEmitter<SortOrder> = new EventEmitter();

    element: any;

    constructor(el: ElementRef) {
        this.element = el.nativeElement;
    }

    sort(field: any) {
        this.resetClasses();
        this.orderChange.next({
            predicate: field,
            ascending: field !== this.order.predicate || !this.order.ascending,
        });
    }

    private resetClasses() {
        const allThIcons = this.element.querySelectorAll(this.sortIconSelector);
        allThIcons.forEach((value) => {
            value.classList.remove(this.sortAscIcon);
            value.classList.remove(this.sortDescIcon);
            value.classList.add(this.sortIcon);
        });
    }
}
