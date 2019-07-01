import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { SourceType } from './source-type.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable()
export class SourceTypeService {

    private resourceUrl = 'api/source-types';

    constructor(private http: HttpClient) {
    }

    create(sourceType: SourceType): Observable<SourceType> {
        const copy: SourceType = Object.assign({}, sourceType);
        return this.http.post(this.resourceUrl, copy) as Observable<SourceType>;
    }

    update(sourceType: SourceType): Observable<SourceType> {
        const copy: SourceType = Object.assign({}, sourceType);
        return this.http.put(this.resourceUrl, copy) as Observable<SourceType>;
    }

    find(producer: string, model: string, version: string): Observable<SourceType> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(producer)}/${encodeURIComponent(model)}/${encodeURIComponent(version)}`) as Observable<SourceType>;
    }

    query(req?: any): Observable<HttpResponse<SourceType[]>> {
        let params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response' }) as Observable<HttpResponse<SourceType[]>>;
    }

    delete(producer: string, model: string, version: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(producer)}/${encodeURIComponent(model)}/${encodeURIComponent(version)}`) as Observable<HttpResponse<any>>;
    }
}
