import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { QueryGroup, QueryParticipant } from '../queries/query.model';

@Injectable({ providedIn: 'root' })
export class QueryParticipantService {
    private baseURL = 'api/query-builder';

    constructor(private http: HttpClient) {}

    getAllQueryGroups(): Observable<QueryGroup[]> {
        return this.http.get<QueryGroup[]>(this.baseURL + '/querygroups');
    }

    assignQueryGroup(queryParticipant: QueryParticipant) {
        return this.http.post(
            this.baseURL + '/assignQueryGroup',
            queryParticipant
        );
    }
}
