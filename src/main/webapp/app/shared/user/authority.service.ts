import { Injectable } from '@angular/core';
import {Http, Response, URLSearchParams} from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Authority } from './authority.model';

@Injectable()
export class AuthorityService {
    private resourceUrl = 'api/authorities';

    constructor(private http: Http) { }

    findAll(): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/`);
    }

    findAllMap(): Observable<Authority[]> {
        return this.http.get(`${this.resourceUrl}/`).map((res) => {
            return res.json();
        });
    }

}
