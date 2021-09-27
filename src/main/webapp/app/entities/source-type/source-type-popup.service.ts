import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { SourceType } from './source-type.model';
import { SourceTypeService } from './source-type.service';

@Injectable({ providedIn: 'root' })
export class SourceTypePopupService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private sourceTypeService: SourceTypeService,
    ) {
    }

    open(component: any, producer?: string, model?: string, version?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (producer && model && version) {
            this.sourceTypeService.find(producer, model, version).subscribe((sourceType) => {
                this.sourceTypeModalRef(component, sourceType);
            });
        } else {
            return this.sourceTypeModalRef(component, new SourceType());
        }
    }

    sourceTypeModalRef(component: any, sourceType: SourceType): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.sourceType = sourceType;
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
