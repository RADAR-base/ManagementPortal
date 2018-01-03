import {Component, Input, OnInit, OnChanges, SimpleChanges} from '@angular/core';

@Component({
    selector: 'show-more',
    template: `
        <div>
            <span *ngFor="let item of items; let last = last">
                <span class="{{spanClass}}">{{item}}</span>
            </span>
        </div>
        <button *ngIf='isCollapsed'
                (click)="expand()"
                type="button"
                class="btn btn-success btn-sm">
            <span class="fa fa-angle-down"></span>
            <span class="hidden-md-down" jhiTranslate="common.showMore">More</span>
        </button>
        <button [hidden]="this.items && this.items.length < this.maxLength"
                *ngIf='!isCollapsed'
                (click)="collapse()"
                type="button"
                class="btn btn-success btn-sm">
            <span class="fa fa-angle-up"></span>
            <span class="hidden-md-down" jhiTranslate="common.showLess">Less</span>
        </button>
    `
})
/**
 * This component collapses if number of array items are more than 10 and allows to expand
 * and collapse in the view.
 */
export class ShowMoreComponent implements OnInit, OnChanges {
    isCollapsed: boolean = false;
    @Input() items?: string[];
    @Input() spanClass : string;
    maxLength =10;

    allItems?: string[];
    ngOnInit() {
        this.itemsChanged();
    }

    ngOnChanges(changes: SimpleChanges) {
        this.itemsChanged();
    }

    expand() {
        this.items = this.allItems;
        this.isCollapsed = false;
    }

    collapse() {
        this.items = this.items.slice(0,this.maxLength);
        this.isCollapsed = true;
    }

    private itemsChanged() {
        this.allItems = this.items;
        if(this.items && this.items.length > this.maxLength){
            this.collapse();
        }
    }
}
