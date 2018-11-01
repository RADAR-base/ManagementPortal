import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { User } from './user.model';

@Injectable()
export class UserService {
    private resourceUrl = 'api/users';

    constructor(private http: Http) {
    }

    create(user: User): Observable<Response> {
        return this.http.post(this.resourceUrl, user);
    }

    update(user: User): Observable<Response> {
        return this.http.put(this.resourceUrl, user);
    }

    find(login: string): Observable<User> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}`).map((res: Response) => res.json());
    }

    findProject(login: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/projects`);
    }

    query(req?: any): Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        if (req) {
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            if (req.authority) {
                params.set('authority', req.authority);
            }
            if (req.projectName) {
                params.set('projectName', req.projectName);
            }
            if (req.login) {
                params.set('login', req.login);
            }
            if (req.email) {
                params.set('email', req.email);
            }
        }

        const options = {
            search: params,
        };

        return this.http.get(this.resourceUrl, options);
    }

    delete(login: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    sendActivation(login: string): Observable<Response> {
        return this.http.post('api/account/reset-activation/init', login);
    }

    findByProjectAndAuthority(req: any): Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        if (req.authority) {
            params.set('authority', req.authority);
        }
        if (req.projectName) {
            params.set('projectName', req.projectName);
        }
        const options = {
            search: params,
        };
        return this.http.get(this.resourceUrl, options);
    }
}
