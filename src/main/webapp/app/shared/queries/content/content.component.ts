import { Component, OnInit } from '@angular/core';
import { ContentItem, ContentType } from '../queries.model';

@Component({
    selector: 'query-content',
    templateUrl: './content.component.html',
    styleUrls: ['./content.component.scss']
})
export class ContentComponent implements OnInit {
    ContentType = ContentType;

    public items: ContentItem[] = [{ id: "idspecial", type: ContentType.PARAGRAPH }]


    constructor() { }

    ngOnInit(): void {
    }



    addContent(contentType: ContentType) {
        const id = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
        this.items.push({ id: id, type: contentType })
    }

    deleteContent(id: string) {
        this.items = this.items.filter(item => item.id !== id);
    }



}
