import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { parseAscending, parsePage, parsePredicate } from './util/pagination-util';

@Injectable()
export class ResolvePagingParams implements Resolve<any> {
    resolve(route: ActivatedRouteSnapshot): PagingParams {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        return {
            page: parsePage(page),
            predicate: parsePredicate(sort),
            ascending: parseAscending(sort),
        };
    }
}

export interface PagingParams {
    page: number;
    predicate: string;
    ascending: boolean;
}
