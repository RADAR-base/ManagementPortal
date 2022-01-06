import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { ProjectService } from '../project/project.service';
import { Source } from './source.model';
import { SourceService } from './source.service';
import { map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SourcePopupService {
    private isOpen = false;

    constructor(
            private modalService: NgbModal,
            private router: Router,
            private sourceService: SourceService,
            private projectService: ProjectService,
    ) {
    }

    open(component: any, sourceName?: string, projectName ?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (sourceName) {
            return this.sourceService.find(sourceName).pipe(
                map((source) => this.sourceModalRef(component, source)),
            );
        } else if (projectName) {
            return this.projectService.find(projectName).pipe(
                map((project) => {
                    const source = new Source();
                    source.project = project;
                    return this.sourceModalRef(component, source);
                }),
            );
        } else {
            return throwError("Cannot create source modal without source or project")
        }
    }

    sourceModalRef(component: any, source: Source): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.source = source;
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
