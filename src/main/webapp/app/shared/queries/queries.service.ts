import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ContentItem } from './queries.model';

@Injectable({ providedIn: 'root' })
export class QueriesService {

    private baseURL = 'api/query-builder';

    constructor(private http: HttpClient) {
    }

    saveContent(queryGroupId: number, contentList: ContentItem[]) {
        return this.http.post(this.baseURL+"/querycontent/querygroup/" + queryGroupId, contentList).toPromise();
    }

    saveContentGroup(data: {
        queryGroupId: number;
        contentGroupName: string;
        items: any[];
      }) {
        return this.http.post(`${this.baseURL}/querycontentgroup`, data).toPromise();
      }
}
