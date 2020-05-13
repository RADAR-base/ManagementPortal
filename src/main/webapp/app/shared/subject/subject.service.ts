import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { Subject } from './subject.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../model/request.utils';

@Injectable()
export class SubjectService {

    private resourceUrl = 'api/subjects';
    private projectResourceUrl = 'api/projects';

    constructor(private http: HttpClient) {
    }

    create(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.post(this.resourceUrl, copy) as Observable<Subject>;
    }

    update(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put(this.resourceUrl, copy) as Observable<Subject>;
    }

    discontinue(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put(`${this.resourceUrl}/discontinue`, copy) as Observable<Subject>;
    }

    find(login: string): Observable<Subject> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}`) as Observable<Subject>;
    }

    findForRevision(login: string, revisionNb: number): Observable<Subject> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions/${revisionNb}`) as Observable<Subject>;
    }

    findRevisions(login: string, req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions`, {params, observe: 'response'}) as Observable<HttpResponse<any>>;

    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    delete(login: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`) as Observable<HttpResponse<any>>;
    }

    findAllByProject(projectName: string, req ?: any): Observable<HttpResponse<Subject[]>> {
        const params = createRequestOption(req);
        return this.http.get(`${this.projectResourceUrl}/${projectName}/subjects`, {params, observe: 'response'}) as Observable<HttpResponse<Subject[]>>;
    }
}
