import {Component, OnInit, Input, Pipe, PipeTransform,} from '@angular/core';
import {
    EventManager,
    AlertService,
    JhiLanguageService
} from 'ng-jhipster';

import {Subscription} from "rxjs/Subscription";
import {Dictionary} from "./dictionary-mapper.model";
import {isNullOrUndefined} from "util";

@Component({
    selector: 'dictionary-mapper',
    templateUrl: './dictionary-mapper.component.html'
})
export class DictionaryMapperComponent implements OnInit {
    @Input() attributes: Dictionary;
    eventSubscriber: Subscription;

    @Input() keys : string[];
    @Input() eventPrefix : string;
    selectedKey: any;
    enteredValue: string;


    constructor(private jhiLanguageService: JhiLanguageService,
                private eventManager: EventManager) {
        this.jhiLanguageService.addLocation('global');
    }

    ngOnInit() {
        if (this.attributes == null) {
            this.attributes = {};
        } else {
            this.keys = this.keys.filter(v => !(v in this.attributes));
        }
        this.update();
    }

    update() {
        this.eventManager.subscribe(this.eventPrefix + 'EditListModification', (response) => {
            this.keys = this.keys.concat(Object.keys(this.attributes));
            this.attributes = response.content;
            this.keys.filter(v => !(v in this.attributes));
        });
    }

    addAttribute() {
        this.attributes[this.selectedKey] = this.enteredValue;
        this.keys = this.keys.filter(v => v !== this.selectedKey);
        this.selectedKey = null;
        this.enteredValue = '';
        this.broadcastAttributes();
    }

    removeAttribute(key: string) {
        delete this.attributes[key];
        this.keys.push(key);
        this.broadcastAttributes();
    }

    private broadcastAttributes(): void {
        this.eventManager.broadcast({
            name: this.eventPrefix + 'ListModification',
            content: this.attributes
        });
    }

    isEmpty(obj: any) {
        for (let key in obj) {
            return false;
        }
        return true;
    }

    trackKey(index: number, item: string) {
        return item;
    }
}

/**
 * Created by nivethika on 30-8-17.
 */
