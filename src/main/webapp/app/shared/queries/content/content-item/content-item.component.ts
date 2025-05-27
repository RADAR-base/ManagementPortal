import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ContentItem, ContentType } from '../../queries.model';
@Component({
    selector: 'app-content-item',
    templateUrl: './content-item.component.html',
    styleUrls: ['./content-item.component.scss']
})
export class ContentItemComponent implements OnInit {
    @Output() triggerDeleteItemFunction = new EventEmitter<string>();
    @Output() triggerUpdateContentFunction = new EventEmitter <{ content: string; index: string }>();

    ContentType = ContentType

    @Input() item: ContentItem

    @Input() index: string;

    ngOnInit(): void {

    }


    updateTextContent(text: string) {
        this.triggerUpdateContentFunction.emit({ content: text, index: this.index });

    }


    deleteItem() {
        this.triggerDeleteItemFunction.emit(this.index);
    }

}
