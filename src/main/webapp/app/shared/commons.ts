import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { parseAscending, parsePage, parsePredicate } from './util/pagination-util';

@Injectable({ providedIn: 'root' })
export class ResolvePagingParams implements Resolve<any> {
    resolve(route: ActivatedRouteSnapshot): PagingParams {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        const params = {
            page: parsePage(page),
            predicate: parsePredicate(sort),
            ascending: parseAscending(sort),
        };
        for (let k in route.queryParams) {
            if (k !== 'page' && k !== 'sort' && route.queryParams[k]) {
                params[k] = route.queryParams[k];
            }
        }
        return params;
    }
}

export interface PagingParams {
    page: number;
    predicate: string;
    ascending: boolean;
    [key: string]: string | number | boolean;
}
