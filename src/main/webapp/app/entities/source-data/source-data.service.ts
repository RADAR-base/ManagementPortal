import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { SourceData } from './source-data.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';
@Injectable()
export class SourceDataService {

    private resourceUrl = 'api/source-data';

    constructor(private http: HttpClient) { }

    create(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.post(this.resourceUrl, copy) as Observable<SourceData>;
    }

    update(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.put(this.resourceUrl, copy) as Observable<SourceData>;
    }

    find(sourceDataName: string): Observable<SourceData> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(sourceDataName)}`) as Observable<SourceData>;
    }

    query(req?: any): Observable<HttpResponse<SourceData[]>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'}) as Observable<HttpResponse<SourceData[]>>;
    }

    delete(sourceDataName: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(sourceDataName)}`) as Observable<HttpResponse<any>>;
    }
}
