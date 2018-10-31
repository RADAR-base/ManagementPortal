import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { OAuthClient } from './oauth-client.model';
import { Subject } from "../../shared/subject";
@Injectable()
export class OAuthClientPairInfoService {

private resourceUrl = 'api/oauth-clients/pair';

constructor(private http: Http) { }

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
