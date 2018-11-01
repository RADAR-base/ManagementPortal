import { Component } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'jhi-subject',
    templateUrl: './general.subject.component.html',
})
export class GeneralSubjectComponent {
    constructor(private jhiLanguateService: JhiLanguageService) {
        this.jhiLanguateService.setLocations([])
    }

    isProjectSpecific = false;
}
