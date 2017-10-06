import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Subject } from './subject.model';
@Injectable()
export class SubjectService {

    private resourceUrl = 'api/subjects';

    constructor(private http: Http) { }

    create(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    discontinue(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put(`${this.resourceUrl}/discontinue`, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(login: string): Observable<Subject> {
        return this.http.get(`${this.resourceUrl}/${login}`).map((res: Response) => {
            return res.json();
        });
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
        ;
    }

    delete(login: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${login}`);
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

    findAllByProject(req ?: any) : Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        if (req) {
            params.set('projectId', req.projectId);
        }
        const options = {
            search: params
        };
        return this.http.get(this.resourceUrl, options);
    }
}
