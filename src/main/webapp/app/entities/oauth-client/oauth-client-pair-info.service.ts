import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Subject } from '../../shared/subject';
import { OAuthClient } from './oauth-client.model';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable({ providedIn: 'root' })
export class OAuthClientPairInfoService {

    private pairUrl = 'api/oauth-clients/pair';
    private resourceUrl = 'api/meta-token';

    constructor(private http: HttpClient) {
    }

    get(client: OAuthClient, subject: Subject, persistent: boolean): Observable<HttpResponse<any>> {
        const params = createRequestOption({
            clientId: client.clientId,
            login: subject.login,
            persistent: persistent.toString(),
        });
        return this.http.get(this.pairUrl, {params, observe: 'response'});
    }

    delete(tokenName: string): Observable<any> {
        return this.http.delete(this.resourceUrl + '/' + tokenName);
    }
}
