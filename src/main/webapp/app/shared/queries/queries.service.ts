import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ContentItem } from './queries.model';

@Injectable({ providedIn: 'root' })
export class QueriesService {

    private baseURL = 'api/query-builder';

    constructor(private http: HttpClient) {
    }

    saveContentGroup(contentGroupData: { queryGroupId: number, contentGroupName: string, queryContentDTOList: ContentItem[] }) {
      return this.http.post(this.baseURL+ "/querygroupcontent", contentGroupData).toPromise();
    }
    
}
