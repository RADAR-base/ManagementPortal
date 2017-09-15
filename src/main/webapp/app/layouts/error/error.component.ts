import {Component, OnDestroy, OnInit} from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import {NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'jhi-error',
    templateUrl: './error.component.html'
})
export class ErrorComponent implements OnInit {
    errorMessage: string;
    error403: boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.setLocations(['error']);
    }

    ngOnInit() {
            this.route.url.subscribe((url) => {
                if(url[0].path==='accessdenied') {
                    this.error403 = true;
                }
            });
    }
}
