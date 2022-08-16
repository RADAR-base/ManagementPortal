import { Directive, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Params } from '@angular/router';
import { switchMap, take } from 'rxjs/operators';

@Directive()
export abstract class ObservablePopupComponent implements OnInit, OnDestroy {
    routeSub: Subscription;
    private modalRef: NgbModalRef;
    private routeParams$: Observable<Params>;

    protected constructor(
        route: ActivatedRoute,
    ) {
        this.routeParams$ = route.params;
    }

    ngOnInit() {
        this.routeSub = this.routeParams$.pipe(
            switchMap(params => this.createModalRef(params)),
            take(1),
        ).subscribe(modalRef => this.modalRef = modalRef);
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
        this.modalRef?.dismiss();
    }

    abstract createModalRef(params: Params): Observable<NgbModalRef>
}
