import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { createRequestOption } from '../../shared/model/request.utils';
import { SourceType } from './source-type.model';

@Injectable({ providedIn: 'root' })
export class SourceTypeService {

    private resourceUrl = 'api/source-types';

    constructor(private http: HttpClient) {
    }

    create(sourceType: SourceType): Observable<SourceType> {
        const copy: SourceType = Object.assign({}, sourceType);
        return this.http.post<SourceType>(this.resourceUrl, copy);
    }

    update(sourceType: SourceType): Observable<SourceType> {
        const copy: SourceType = Object.assign({}, sourceType);
        return this.http.put<SourceType>(this.resourceUrl, copy);
    }

    find(producer: string, model: string, version: string): Observable<SourceType> {
        return this.http.get<SourceType>(`${this.resourceUrl}/${encodeURIComponent(producer)}/${encodeURIComponent(model)}/${encodeURIComponent(version)}`);
    }

    query(req?: any): Observable<HttpResponse<SourceType[]>> {
        const params = createRequestOption(req);
        return this.http.get<SourceType[]>(this.resourceUrl, {params, observe: 'response' });
    }

    delete(producer: string, model: string, version: string): Observable<object> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(producer)}/${encodeURIComponent(model)}/${encodeURIComponent(version)}`);
    }
}
