import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BehaviorSubject, combineLatest, Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';

import {Organization, OrganizationService} from '../../shared';
import {regularSortOrder, SortOrder, SortOrderImpl} from '../../shared/util/sort-util';

@Component({
    selector: 'jhi-organization',
    templateUrl: './organization.component.html',
})
export class OrganizationComponent implements OnInit, OnDestroy {
    organizations$: Observable<Organization[]>;
    sortOrder$: Observable<SortOrderImpl>;
    subscriptions: Subscription = new Subscription();
    private _sortOrder$ = new BehaviorSubject<SortOrder>({predicate: 'name', ascending: true});

    constructor(
        public organizationService: OrganizationService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
        this.sortOrder$ = this._sortOrder$.pipe(regularSortOrder());
    }

    ngOnInit() {
        this.subscriptions.add(this.registerChangeInParams());
        this.subscriptions.add(this.registerChangeInSortOrder());

        this.organizations$ = combineLatest([
            this.organizationService.organizations$,
            this.sortOrder$,
        ]).pipe(
            map(([organizations, order]) => order.sort(organizations)),
        );
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    sort(order: SortOrder) {
        console.log('sort', order);
        this._sortOrder$.next(order);
    }

    trackName(index: number, item: Organization) {
        return item.name;
    }

    private registerChangeInParams(): Subscription {
        return this.activatedRoute.data.subscribe(data => {
            this._sortOrder$.next(data['pagingParams']);
        });
    }

    private registerChangeInSortOrder(): Subscription {
        return this.sortOrder$.subscribe(
            order => this.router.navigate(['/organization'], {
                queryParams: {
                    sort: order.predicate + ',' + (order.ascending ? 'asc' : 'desc'),
                },
            }),
        );
    }
}
