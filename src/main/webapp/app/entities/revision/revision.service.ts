import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Revision } from './revision.model';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable({ providedIn: 'root' })
export class RevisionService {
    private resourceUrl = 'api/revisions';

    constructor(private http: HttpClient) {
    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'});
    }

    find(id: number): Observable<Revision> {
        return this.http.get<Revision>(`${this.resourceUrl}/${id}`);
    }
}
