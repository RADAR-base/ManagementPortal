import { Component, Input, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { EventManager } from '../util/event-manager.service';

@Component({
    selector: 'jhi-dictionary-mapper',
    templateUrl: './dictionary-mapper.component.html',
})
export class DictionaryMapperComponent implements OnInit {
    @Input() attributes: Record<string, string>;
    eventSubscriber: Subscription;

    @Input() options: string[];
    @Input() eventPrefix: string;
    selectedKey: any;
    enteredValue: string;

    constructor(
        private eventManager: EventManager,
    ) {
        this.selectedKey = null;
        this.enteredValue = '';
    }

    ngOnInit() {
        if (this.attributes === undefined) {
            this.attributes = {};
        }
        this.eventManager.subscribe(this.eventPrefix + 'EditListModification', (response) => {
            this.attributes = response.content;
        });
    }

    addAttribute() {
        this.attributes[this.selectedKey] = this.enteredValue;
        this.selectedKey = null;
        this.enteredValue = '';
        this.broadcastAttributes();
    }

    removeAttribute(key: string) {
        delete this.attributes[key];
        this.broadcastAttributes();
    }

    activeOptions(): string[] {
        return this.options.filter(o => !(o in this.attributes));
    }

    private broadcastAttributes(): void {
        this.eventManager.broadcast({
            name: this.eventPrefix + 'ListModification',
            content: this.attributes,
        });
    }

    isEmpty(obj: any) {
        return !obj || Object.keys(obj).length === 0;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }
}
