import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Revision } from './revision.model';

@Injectable()
export class RevisionService {
private resourceUrl = 'api/revisions';

constructor(private http: Http) { }
    query(req?: any): Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        if (req) {
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
        }

        const options = {
            search: params
        };

        return this.http.get(this.resourceUrl, options);
    }
}