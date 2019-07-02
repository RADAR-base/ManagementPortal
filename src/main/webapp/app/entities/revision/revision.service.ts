import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { Revision } from './revision.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable()
export class RevisionService {
    private resourceUrl = 'api/revisions';

    constructor(private http: HttpClient) {
    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    find(id: number): Observable<Revision> {
        return this.http.get(`${this.resourceUrl}/${id}`) as Observable<Revision>;
    }
}
