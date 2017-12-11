import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'show-more',
    template: `
        <div >
            <span *ngFor="let scope of scopes; let last = last"><span class="badge badge-primary">{{scope}}</span>{{last ? "" : "&nbsp;"}}</span>
        </div>
        <button *ngIf='isCollapsed'
                (click)="expand()"
                type="button"
                class="btn btn-success btn-sm">
            <span class="fa fa-angle-down"></span>
            <span class="hidden-md-down" jhiTranslate="common.showMore">More</span>
        </button>
        <button [hidden]="this.scopes && this.scopes.length < 10"
                *ngIf='!isCollapsed'
                (click)="collapse()"
                type="button"
                class="btn btn-success btn-sm">
            <span class="fa fa-angle-up"></span>
            <span class="hidden-md-down" jhiTranslate="common.showLess">Less</span>
        </button>
    `,
    styles: [`
        div.collapsed {
            overflow: hidden;
        }
    `]
})
/**
 * This component collapses if number of array items are more than 10 and allows to expand
 * and collapse in the view.
 */
export class ShowMoreComponent implements OnInit{
    isCollapsed: boolean = false;
    @Input() scopes?: string[];

    allScopes?: string[];
    ngOnInit() {
        this.allScopes = this.scopes;
        if(this.scopes && this.scopes.length > 10){
            this.collapse();
        }
    }

    expand() {
        this.scopes = this.allScopes;
        this.isCollapsed = false;
    }

    collapse() {
        this.scopes = this.scopes.slice(0,10);
        this.isCollapsed = true;
    }
}
