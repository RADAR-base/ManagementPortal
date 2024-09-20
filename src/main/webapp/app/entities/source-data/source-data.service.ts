import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {SourceData} from './source-data.model';
import {createRequestOption} from '../../shared/model/request.utils';

@Injectable({providedIn: 'root'})
export class SourceDataService {

    private resourceUrl = 'api/source-data';

    constructor(private http: HttpClient) {
    }

    create(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.post<SourceData>(this.resourceUrl, copy);
    }

    update(sourceData: SourceData): Observable<SourceData> {
        const copy: SourceData = Object.assign({}, sourceData);
        return this.http.put<SourceData>(this.resourceUrl, copy);
    }

    find(sourceDataName: string): Observable<SourceData> {
        return this.http.get<SourceData>(`${this.resourceUrl}/${encodeURIComponent(sourceDataName)}`);
    }

    query(req?: any): Observable<HttpResponse<SourceData[]>> {
        const params = createRequestOption(req);
        return this.http.get<SourceData[]>(this.resourceUrl, {params, observe: 'response'});
    }

    delete(sourceDataName: string): Observable<object> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(sourceDataName)}`);
    }
}
