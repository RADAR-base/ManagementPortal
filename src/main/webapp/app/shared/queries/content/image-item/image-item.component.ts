import { Component, OnInit, Input } from '@angular/core';
import { ContentItem } from '../../queries.model';

@Component({
    selector: 'query-image-item',
    templateUrl: './image-item.component.html',
    styleUrls: ['./image-item.component.scss']
})
export class ImageItemComponent implements OnInit {
    public imageValue: String

    @Input() item: ContentItem

    constructor() { }

    ngOnInit(): void {
    }
}
