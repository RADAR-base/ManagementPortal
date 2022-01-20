import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { SourceData } from './source-data.model';
import { SourceDataService } from './source-data.service';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class SourceDataPopupService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private sourceDataService: SourceDataService,
    ) {
    }

    open(component: any, sourceDataName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (sourceDataName) {
            return this.sourceDataService.find(sourceDataName).pipe(
                map((sourceData) => this.sourceDataModalRef(component, sourceData)),
            );
        } else {
            return of(this.sourceDataModalRef(component, new SourceData()));
        }
    }

    sourceDataModalRef(component: any, sourceData: SourceData): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.sourceData = sourceData;
        modalRef.result.then((result) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
