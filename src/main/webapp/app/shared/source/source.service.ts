import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { MinimalSource, Source } from './source.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../model/request.utils';

@Injectable()
export class SourceService {

    private resourceUrl = 'api/sources';
    private projectResourceUrl = 'api/projects';

    constructor(private http: HttpClient) {
    }

    create(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.post(this.resourceUrl, copy) as Observable<Source>;
    }

    update(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.put(this.resourceUrl, copy) as Observable<Source>;
    }

    find(name: string): Observable<Source> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(name)}`) as Observable<Source>;
    }

    query(req?: any): Observable<HttpResponse<Source[]>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'}) as Observable<HttpResponse<Source[]>>;
    }

    delete(sourceName: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(sourceName)}`) as Observable<HttpResponse<any>>;
    }

    findAllByProject(req?: any): Observable<HttpResponse<Source[]>> {
        const params = createRequestOption(req);
        return this.http.get(`${this.projectResourceUrl}/${req.projectName}/sources`, {params, observe: 'response'}) as Observable<HttpResponse<Source[]>>;
    }

    findAvailable(projectName: string): Observable<HttpResponse<MinimalSource[]>> {
        const params: any = {
            assigned: false,
            minimized: true
        };
        return this.http.get(`${this.projectResourceUrl}/${projectName}/sources`, {params, observe: 'response'}) as Observable<HttpResponse<MinimalSource[]>>;
    }
}
