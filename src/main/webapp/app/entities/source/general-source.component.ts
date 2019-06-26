import { Component } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'jhi-source',
    templateUrl: './general-source.component.html',
})
export class GeneralSourceComponent {
    isProjectSpecific = false;

    constructor(
            private jhiLanguageService: JhiLanguageService,
    ) {
        jhiLanguageService.setLocations(['source'])
    }
}
