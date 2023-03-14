import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Subject } from '../../shared/subject';
import { OAuthClient, PairInfo } from './oauth-client.model';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable({ providedIn: 'root' })
export class OAuthClientPairInfoService {

    private pairUrl = 'api/oauth-clients/pair';
    private resourceUrl = 'api/meta-token';

    constructor(private http: HttpClient) {
    }

    get(client: OAuthClient, subject: Subject, persistent: boolean): Observable<PairInfo> {
        const params = createRequestOption({
            clientId: client.clientId,
            login: subject.login,
            persistent: persistent.toString(),
        });
        return this.http.get<PairInfo>(this.pairUrl, {params});
    }

    delete(tokenName: string): Observable<HttpResponse<any>> {
        return this.http.delete(this.resourceUrl + '/' + tokenName, { observe: 'response' });
    }
}
