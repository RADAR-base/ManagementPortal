import { Injectable } from '@angular/core';
import { BaseRequestOptions, Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Subject } from '../../shared/subject';

import { OAuthClient } from './oauth-client.model';

@Injectable()
export class OAuthClientPairInfoService {

    private resourceUrl = 'api/oauth-clients/pair';

    constructor(private http: Http) {
    }

    get(client: OAuthClient, subject: Subject): Observable<Response> {
        const options = this.createRequestOption(client.clientId, subject.login);
        return this.http.get(this.resourceUrl, options);
    }

    private createRequestOption(clientId: string, subjectLogin: string): BaseRequestOptions {
        const options: BaseRequestOptions = new BaseRequestOptions();
        const params: URLSearchParams = new URLSearchParams();
        params.set('clientId', clientId);
        params.set('login', subjectLogin);
        options.params = params;
        return options;
    }
}
