import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ContentItem, ContentType } from '../../queries.model';
@Component({
    selector: 'app-content-item',
    templateUrl: './content-item.component.html',
    styleUrls: ['./content-item.component.scss']
})
export class ContentItemComponent implements OnInit {
    @Output() triggerDeleteItemFunction = new EventEmitter<string>();

    ContentType = ContentType

    content = "hello world"

    @Input() item: ContentItem

    ngOnInit(): void {
    }


    deleteItem(id: string) {
        this.triggerDeleteItemFunction.emit(id);
    }

}
