import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import {Role} from "./role.model";
@Injectable()
export class RoleService {

    private resourceUrl = 'api/roles';

    constructor(private http: Http) { }

    create(role: Role): Observable<Role> {
        const copy: Role = Object.assign({}, role);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(role: Role): Observable<Role> {
        const copy: Role = Object.assign({}, role);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(id: number): Observable<Role> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            return res.json();
        });
    }

    findByProject(projectId: number): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/project/${projectId}`);
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
        ;
    }

    findAdminRoles(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/admin`, options)
            ;
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }
    private createRequestOption(req?: any): BaseRequestOptions {
        const options: BaseRequestOptions = new BaseRequestOptions();
        if (req) {
            const params: URLSearchParams = new URLSearchParams();
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            params.set('query', req.query);

            options.search = params;
        }
        return options;
    }
}
