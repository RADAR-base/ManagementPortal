import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { LoginModalService } from '../../shared';

@Component({
    selector: 'jhi-error',
    templateUrl: './error.component.html',
})
export class ErrorComponent implements OnInit {
    errorMessage: string;
    error403: boolean;
    modalRef: NgbModalRef;

    constructor(
            private jhiLanguageService: JhiLanguageService,
            private loginModalService: LoginModalService,
            private route: ActivatedRoute,
    ) {
        this.jhiLanguageService.setLocations(['error']);
    }

    ngOnInit() {
        this.route.url.subscribe((url) => {
            if (url[0].path === 'accessdenied') {
                this.error403 = true;
            }
            // this.login();
        });
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
