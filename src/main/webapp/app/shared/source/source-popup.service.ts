import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Source } from './source.model';
import { SourceService } from './source.service';
import {ProjectService} from "../../entities/project/project.service";
@Injectable()
export class SourcePopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private sourceService: SourceService,
        private projectService: ProjectService

    ) {}

    open(component: Component, id?: number | any , projectId ?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.sourceService.find(id).subscribe((source) => {
                this.sourceModalRef(component, source);
            });
        } else {
            if(projectId) {
                this.projectService.find(projectId).subscribe((project) => {
                    let source = new Source();
                    source.project = project;
                    return this.sourceModalRef(component, source );
                })
            }
        }
    }

    sourceModalRef(component: Component, source: Source): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.source = source;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        });
        return modalRef;
    }
}
