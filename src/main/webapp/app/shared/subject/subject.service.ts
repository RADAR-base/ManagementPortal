import { Injectable } from '@angular/core';
import { BaseRequestOptions, Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Subject } from './subject.model';
import { ResponseWrapper, createRequestOption } from '..';

@Injectable()
export class SubjectService {

    private resourceUrl = 'api/subjects';
    private projectResourceUrl = 'api/projects';
    private resourceSearchUrl = 'api/_search/subjects';

    constructor(private http: Http) {
    }

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
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}`).map((res: Response) => {
            return res.json();
        });
    }

    findForRevision(login: string, revisionNb: number): Observable<Subject> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions/${revisionNb}`).map((res: Response) => {
            return res.json();
        });
    }

    findRevisions(login: string, req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions`);
    }

    query(req?: any): Observable<Response> {
        const options = this.createRequestOption(req);
        return this.http.get(this.resourceUrl, options);
    }

    delete(login: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
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

    findAllByProject(req ?: any): Observable<Response> {
        const params: URLSearchParams = new URLSearchParams();
        params.set('page', req.page);
        params.set('size', req.size);
        if (req.sort) {
            params.paramsMap.set('sort', req.sort);
        }
        params.set('query', req.query);

        const options = {
            search: params,
        };
        return this.http.get(`${this.projectResourceUrl}/${req.projectName}/subjects`, options);
    }

    search(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options)
                .map((res: any) => this.convertResponse(res));
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }

    /**
     * Convert a returned JSON object to Subject.
     */
    private convertItemFromServer(json: any): Subject {
        const entity: Subject = Object.assign(new Subject(), json);
        return entity;
    }
}
