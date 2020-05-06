import { Injectable } from '@angular/core';
import { BaseRequestOptions, Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Subject } from '../../shared/subject';

import { OAuthClient } from './oauth-client.model';

@Injectable()
export class OAuthClientPairInfoService {

    private pairUrl = 'api/oauth-clients/pair';
    private resourceUrl = 'api/meta-token';

    constructor(private http: Http) {
    }

    get(client: OAuthClient, subject: Subject, persistent: boolean): Observable<Response> {
        const options = this.createRequestOption(client.clientId, subject.login, persistent);
        return this.http.get(this.pairUrl, options);
    }

    delete(tokenName: string): Observable<Response> {
        return this.http.delete(this.resourceUrl + '/' + tokenName);
    }

    private createRequestOption(clientId: string, subjectLogin: string, persistent: boolean): BaseRequestOptions {
        const options: BaseRequestOptions = new BaseRequestOptions();
        const params: URLSearchParams = new URLSearchParams();
        params.set('clientId', clientId);
        params.set('login', subjectLogin);
        params.set('persistent', persistent.toString());
        options.params = params;
        return options;
    }
}
