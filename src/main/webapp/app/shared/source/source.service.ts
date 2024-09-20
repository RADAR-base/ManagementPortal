import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {MinimalSource, Source} from './source.model';
import {createRequestOption} from '../model/request.utils';

@Injectable({providedIn: 'root'})
export class SourceService {

    private resourceUrl = 'api/sources';
    private projectResourceUrl = 'api/projects';

    constructor(private http: HttpClient) {
    }

    create(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.post<Source>(this.resourceUrl, copy);
    }

    update(source: Source): Observable<Source> {
        const copy: Source = Object.assign({}, source);
        return this.http.put<Source>(this.resourceUrl, copy);
    }

    find(name: string): Observable<Source> {
        return this.http.get<Source>(`${this.resourceUrl}/${encodeURIComponent(name)}`);
    }

    query(req?: any): Observable<HttpResponse<Source[]>> {
        const params = createRequestOption(req);
        return this.http.get<Source[]>(this.resourceUrl, {params, observe: 'response'});
    }

    delete(sourceName: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(sourceName)}`);
    }

    findAllByProject(req?: any): Observable<HttpResponse<Source[]>> {
        const params = createRequestOption(req);
        return this.http.get<Source[]>(`${this.projectResourceUrl}/${req.projectName}/sources`, {
            params,
            observe: 'response'
        });
    }

    findAvailable(projectName: string): Observable<HttpResponse<MinimalSource[]>> {
        const params: any = {
            assigned: false,
            minimized: true
        };
        return this.http.get<MinimalSource[]>(`${this.projectResourceUrl}/${projectName}/sources`, {
            params,
            observe: 'response'
        });
    }
}
