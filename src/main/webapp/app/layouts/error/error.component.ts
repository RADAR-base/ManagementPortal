import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

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
            private loginModalService: LoginModalService,
            private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.route.url.subscribe((url) => {
            if (url[0].path === 'accessdenied') {
                this.error403 = true;
            }
        });
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }
}
