import { Component, OnInit } from '@angular/core';
import { ContentItem, ContentType } from '../queries.model';

@Component({
    selector: 'query-content',
    templateUrl: './content.component.html',
    styleUrls: ['./content.component.scss']
})
export class ContentComponent implements OnInit {
    ContentType = ContentType;

    public items: [ContentItem?] = []


    constructor() { }

    ngOnInit(): void {
    }



    addContent(contentType: ContentType) {
        console.log("content is being added" + contentType)

        this.items.push({ type: contentType })
    }



}
