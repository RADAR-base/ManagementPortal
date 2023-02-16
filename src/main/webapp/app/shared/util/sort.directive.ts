// Borrowed from ng-jhipster

import { Directive, ElementRef, EventEmitter, Input, Output } from '@angular/core';

@Directive({
    selector: '[jhiSort]'
})
export class JhiSortDirective {
    @Input() predicate: string;
    @Input() ascending: boolean;
    @Input() callback: Function;

    sortIcon = 'fa-sort';
    sortAscIcon = 'fa-sort-asc';
    sortDescIcon = 'fa-sort-desc';
    sortIconSelector = 'span.fa';

    @Output() predicateChange: EventEmitter<any> = new EventEmitter();
    @Output() ascendingChange: EventEmitter<any> = new EventEmitter();

    element: any;

    constructor(el: ElementRef) {
        this.element = el.nativeElement;
    }

    sort(field: any) {
        this.resetClasses();
        if (field !== this.predicate) {
            this.ascending = true;
        } else {
            this.ascending = !this.ascending;
        }
        this.predicate = field;
        this.predicateChange.emit(field);
        this.ascendingChange.emit(this.ascending);
        if (this.callback) {
            this.callback();
        }
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
