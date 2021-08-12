// Borrowed from ng-jhipster

import { Directive, Host, HostListener, Input, ElementRef, Renderer2 } from '@angular/core';
import { JhiSortDirective } from './sort.directive';

@Directive({
    selector: '[jhiSortBy]'
})
export class JhiSortByDirective {
    @Input() jhiSortBy: string;

    sortAscIcon = 'fa-sort-asc';
    sortDescIcon = 'fa-sort-desc';

    jhiSort: JhiSortDirective;

    constructor(
        @Host() jhiSort: JhiSortDirective,
        private el: ElementRef,
        private renderer: Renderer2,
    ) {
        this.jhiSort = jhiSort;
    }

    @HostListener('click') onClick() {
        if (this.jhiSort.predicate && this.jhiSort.predicate !== '_score') {
            this.jhiSort.sort(this.jhiSortBy);
            this.applyClass();
        }
    }

    private applyClass () {
        let childSpan = this.el.nativeElement.children[1];
        let add = this.sortAscIcon;
        if (!this.jhiSort.ascending) {
            add = this.sortDescIcon;
        }
        this.renderer.addClass(childSpan, add);
    };
}