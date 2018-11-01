import { Component, Input, OnInit } from '@angular/core';
import { AlertService, EventManager, JhiLanguageService } from 'ng-jhipster';

import { Subscription } from 'rxjs/Subscription';
import { Attribute } from './attribute-mapper.model';

@Component({
    selector: 'jhi-attribute-mapper',
    templateUrl: './attribute-mapper.component.html',
})
export class AttributeMapperComponent implements OnInit {
    @Input() attributes: Attribute[];
    eventSubscriber: Subscription;

    @Input() keys: string[];
    @Input() eventPrefix: string;
    selectedKey: any;
    enteredValue: string;

    constructor(private jhiLanguageService: JhiLanguageService,
                private alertService: AlertService,
                private eventManager: EventManager) {
        this.jhiLanguageService.addLocation('global');
    }

    ngOnInit() {
        if (this.attributes === null) {
            this.attributes = [];
        }
        this.registerChangeInParentComponent();
    }

    registerChangeInParentComponent() {
        this.eventManager.subscribe(this.eventPrefix + 'EditListModification', (response) => {
            this.attributes = response.content;
        });
    }

    trackId(index: number, item: Attribute) {
        return item.key;
    }

    addAttribute() {
        const newAttributeData = new Attribute();
        newAttributeData.key = this.selectedKey;
        newAttributeData.value = this.enteredValue;
        if (this.hasAttribute(newAttributeData)) {
            this.alertService.error('global.attribute.error.alreadyExist', null, null);
        } else {
            this.attributes.push(newAttributeData);
        }
        this.eventManager.broadcast({
            name: this.eventPrefix + 'ListModification',
            content: this.attributes,
        });
    }

    hasAttribute(attribute: Attribute): boolean {
        return this.attributes.some(v => v.key === attribute.key);
    }

    removeAttribute(attribute: Attribute) {
        this.attributes.splice(this.attributes.indexOf(v => v.key === attribute.key), 1);
        this.eventManager.broadcast({
            name: this.eventPrefix + 'ListModification',
            content: this.attributes,
        });
    }

}

/**
 * Created by nivethika on 30-8-17.
 */
