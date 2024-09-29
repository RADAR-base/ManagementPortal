// Borrowed from ng-jhipster

import {Directive, ElementRef, Host, HostListener, Input, Renderer2} from '@angular/core';
import {JhiSortOrderDirective} from "./sort-order.directive";

@Directive({
    selector: '[jhiOrderBy]'
})
export class JhiOrderByDirective {
    @Input() jhiOrderBy: string;

    sortAscIcon = 'fa-sort-asc';
    sortDescIcon = 'fa-sort-desc';

    jhiSort: JhiSortOrderDirective;

    constructor(
        @Host() jhiSort: JhiSortOrderDirective,
        private el: ElementRef,
        private renderer: Renderer2,
    ) {
        this.jhiSort = jhiSort;
    }

    @HostListener('click') onClick() {
        if (this.jhiSort.order.predicate && this.jhiSort.order.predicate !== '_score') {
            this.jhiSort.sort(this.jhiOrderBy);
            this.applyClass();
        }
    }

    private applyClass() {
        let childSpan = this.el.nativeElement.children[1];
        let add = this.sortAscIcon;
        if (!this.jhiSort.order.ascending) {
            add = this.sortDescIcon;
        }
        this.renderer.addClass(childSpan, add);
    };
}
