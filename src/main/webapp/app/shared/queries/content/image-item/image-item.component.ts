import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ContentItem } from '../../queries.model';

@Component({
    selector: 'query-image-item',
    templateUrl: './image-item.component.html',
    styleUrls: ['./image-item.component.scss']
})
export class ImageItemComponent implements OnInit {
    @Output() triggerDeleteItemFunction = new EventEmitter<string>();

    public imageValue: String

    @Input() item: ContentItem

    ngOnInit(): void {
    }

    onDeleteItem() {
        this.triggerDeleteItemFunction.emit()
    }
}
