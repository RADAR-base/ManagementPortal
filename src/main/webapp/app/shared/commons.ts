import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { parseSort, parsePage } from './util/pagination-util';

@Injectable({ providedIn: 'root' })
export class ResolvePagingParams implements Resolve<any> {
    resolve(route: ActivatedRouteSnapshot): PagingParams {
        let { page, sort, ...otherParams } = route.queryParams;
        let { predicate, ascending } = parseSort(sort || 'id,asc')
        return {
            page: parsePage(page || '1'),
            predicate,
            ascending,
            ...otherParams,
        };
    }
}

export interface PagingParams {
    page: number;
    predicate: string;
    ascending: boolean;
    [key: string]: string | number | boolean;
}
