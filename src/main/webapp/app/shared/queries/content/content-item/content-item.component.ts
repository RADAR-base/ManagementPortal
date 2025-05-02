import { Component, OnInit, Input } from '@angular/core';
import { ContentItem, ContentType } from '../../queries.model';
@Component({
  selector: 'app-content-item',
  templateUrl: './content-item.component.html',
  styleUrls: ['./content-item.component.scss']
})
export class ContentItemComponent implements OnInit {
 ContentType = ContentType

    content = "hello world"

    @Input() item : ContentItem

    public editorUUID: String = "";

    constructor() {
        const id = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
        this.editorUUID = id;
    }


  ngOnInit(): void {
  }

}
