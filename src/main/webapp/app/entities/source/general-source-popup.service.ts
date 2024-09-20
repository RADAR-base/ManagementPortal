import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {Source, SourceService} from '../../shared/source';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable()
export class GeneralSourcePopupService {
    private isOpen = false;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private sourceService: SourceService,
    ) {
    }

    open(component: any, sourceName?: string): Observable<NgbModalRef> {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (sourceName) {
            return this.sourceService.find(sourceName).pipe(
                map((source) => this.sourceModalRef(component, source)),
            );
        } else {
            return of(this.sourceModalRef(component, new Source()));
        }
    }

    sourceModalRef(component: any, source: Source): NgbModalRef {
        const modalRef = this.modalService.open(component, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.source = source;
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
