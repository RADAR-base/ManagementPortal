import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subject, Subscription} from 'rxjs';
import {map, startWith} from 'rxjs/operators';

import {SourceType} from './source-type.model';
import {SourceTypeService} from './source-type.service';

@Component({
    selector: 'jhi-source-type',
    templateUrl: './source-type.component.html',
})
export class SourceTypeComponent implements OnInit, OnDestroy {
    predicate: any;
    ascending: any;
    sourceTypes$: Observable<SourceType[]>;
    private subscriptions = new Subscription();
    private trigger$ = new Subject<void>();

    constructor(
        private sourceTypeService: SourceTypeService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
        this.sourceTypes$ = combineLatest([
            this.sourceTypeService.sourceTypes$,
            this.trigger$.pipe(startWith(undefined as void))
        ]).pipe(
            map(([types]) => SourceTypeComponent.sortByPredicate(
                types, t => t[this.predicate], this.ascending
            )),
        );
    }

    private static sortByPredicate<T>(values: T[], predicate: (T) => any, ascending: boolean): T[] {
        const modifier = ascending ? 1 : -1;
        const localValues = [...values];
        localValues.sort((t1, t2) => {
            const v1 = predicate(t1);
            const v2 = predicate(t2);
            if (v1 === v2 || !v1 && !v2) {
                return 0;
            } else if (!v1) {
                return -modifier;
            } else if (!v2) {
                return modifier;
            }
            return modifier * v1.toLocaleString().localeCompare(v2);
        });
        return localValues;
    }

    ngOnInit() {
        this.subscriptions.add(this.activatedRoute.data.pipe(
            map(data => {
                const fallback = {page: 1, predicate: 'id', ascending: true};
                return data['pagingParams'] || fallback;
            }),
        ).subscribe(params => {
            this.ascending = params.ascending;
            this.predicate = params.predicate;
            this.trigger$.next();
        }));
    }

    ngOnDestroy() {
        this.trigger$.complete();
        this.subscriptions.unsubscribe();
    }

    trackId(index: number, item: SourceType) {
        return item.id;
    }

    transition() {
        this.router.navigate(['/source-type'], {
            queryParams: {
                sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
            },
        });
        this.trigger$.next();
    }
}
