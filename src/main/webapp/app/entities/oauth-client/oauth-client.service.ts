import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { OAuthClient } from './oauth-client.model';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable({ providedIn: 'root' })
export class OAuthClientService {

    private resourceUrl = 'api/oauth-clients';

    constructor(private http: HttpClient) {
    }

    create(client: OAuthClient): Observable<OAuthClient> {
        const copy: OAuthClient = Object.assign({}, client);
        return this.http.post<OAuthClient>(this.resourceUrl, copy);
    }

    update(client: OAuthClient): Observable<OAuthClient> {
        const copy: OAuthClient = Object.assign({}, client);
        return this.http.put<OAuthClient>(this.resourceUrl, copy);
    }

    find(id: string): Observable<OAuthClient> {
        return this.http.get<OAuthClient>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'});
    }

    delete(id: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(id)}`);
    }
}
