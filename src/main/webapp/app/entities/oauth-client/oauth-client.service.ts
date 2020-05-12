import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { OAuthClient } from './oauth-client.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable()
export class OAuthClientService {

    private resourceUrl = 'api/oauth-clients';

    constructor(private http: HttpClient) {
    }

    create(client: OAuthClient): Observable<OAuthClient> {
        const copy: OAuthClient = Object.assign({}, client);
        return this.http.post(this.resourceUrl, copy) as Observable<OAuthClient>;
    }

    update(client: OAuthClient): Observable<OAuthClient> {
        const copy: OAuthClient = Object.assign({}, client);
        return this.http.put(this.resourceUrl, copy) as Observable<OAuthClient>;
    }

    find(id: string): Observable<OAuthClient> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(id)}`) as Observable<OAuthClient>;
    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'});
    }

    delete(id: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(id)}`) as Observable<HttpResponse<any>>;
    }
}
