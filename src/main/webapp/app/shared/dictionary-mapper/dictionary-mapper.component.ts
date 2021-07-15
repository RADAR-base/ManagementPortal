import { Component, Input, OnInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';

import { Subscription } from 'rxjs/Subscription';
import { EventManager } from '../util/event-manager.service';
import { Dictionary } from './dictionary-mapper.model';

@Component({
    selector: 'jhi-dictionary-mapper',
    templateUrl: './dictionary-mapper.component.html',
})
export class DictionaryMapperComponent implements OnInit {
    @Input() attributes: Dictionary;
    eventSubscriber: Subscription;

    @Input() options: string[];
    @Input() eventPrefix: string;
    selectedKey: any;
    enteredValue: string;

    constructor(private jhiLanguageService: JhiLanguageService,
                private eventManager: EventManager) {
        this.jhiLanguageService.addLocation('global');
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
        if (obj) {
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    trackKey(index: number, item: any) {
        return item.key;
    }
}
