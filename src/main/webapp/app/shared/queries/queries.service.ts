import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Account } from "../user/account.model";
import { ContentItem } from './queries.model';

@Injectable({ providedIn: 'root' })
export class QueriesService {
    constructor(private http: HttpClient) {
    }

    // get(): Observable<Account> {
    //     return this.http.get<Account>('api/account');
    // }

    saveContent(queryGroupId: number, contentList: ContentItem[]) {
        return this.http.post('api/querycontent/querygroup/' + queryGroupId, contentList).toPromise();
    }
}
