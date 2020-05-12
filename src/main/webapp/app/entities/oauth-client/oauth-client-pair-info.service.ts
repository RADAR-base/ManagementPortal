import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Subject } from '../../shared/subject';

import { OAuthClient } from './oauth-client.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable()
export class OAuthClientPairInfoService {

    private resourceUrl = 'api/oauth-clients/pair';

    constructor(private http: HttpClient) {
    }

    get(client: OAuthClient, subject: Subject): Observable<HttpResponse<any>> {
        const params = createRequestOption({clientId: client.clientId, login: subject.login} );
        return this.http.get(this.resourceUrl, {params, observe: 'response'});
    }
}
