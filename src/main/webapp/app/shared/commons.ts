import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { PaginationUtil } from 'ng-jhipster';

@Injectable()
export class ResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: PaginationUtil) {
    }

    resolve(route: ActivatedRouteSnapshot): PagingParams {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        return {
            page: this.paginationUtil.parsePage(page),
            predicate: this.paginationUtil.parsePredicate(sort),
            ascending: this.paginationUtil.parseAscending(sort),
        };
    }
}

export interface PagingParams {
    page: number;
    predicate: string;
    ascending: boolean;
}
