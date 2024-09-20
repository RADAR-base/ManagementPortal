import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {SourceType} from './source-type.model';
import {SourceTypeService} from './source-type.service';
import {Observable, of} from "rxjs";
import {catchError, filter, map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class SourceTypePopupService {
    private isOpen = false;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private sourceTypeService: SourceTypeService,
    ) {
    }

    open(component: any, producer?: string, model?: string, version?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (producer && model && version) {
            return this.sourceTypeService.find(producer, model, version).pipe(
                filter(s => !!s),
                map(sourceType => this.sourceTypeModalRef(component, sourceType)),
                catchError(() => of(this.sourceTypeModalRef(component, {}))),
            )
        } else {
            return of(this.sourceTypeModalRef(component, {}));
        }
    }

    sourceTypeModalRef(component: any, sourceType: SourceType): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.sourceType = sourceType || {};
        modalRef.result.then(() => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        }, () => {
            this.router.navigate([{outlets: {popup: null}}], {replaceUrl: true});
            this.isOpen = false;
        });
        return modalRef;
    }
}
