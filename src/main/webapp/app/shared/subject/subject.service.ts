import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Subject } from './subject.model';
import { createRequestOption } from '../model/request.utils';

@Injectable({ providedIn: 'root' })
export class SubjectService {

    private resourceUrl = 'api/subjects';
    private projectResourceUrl = 'api/projects';

    constructor(private http: HttpClient) {
    }

    create(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.post<Subject>(this.resourceUrl, copy);
    }

    update(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put<Subject>(this.resourceUrl, copy);
    }

    discontinue(subject: Subject): Observable<Subject> {
        const copy: Subject = Object.assign({}, subject);
        return this.http.put<Subject>(`${this.resourceUrl}/discontinue`, copy);
    }

    find(login: string): Observable<Subject> {
        return this.http.get<Subject>(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    findForRevision(login: string, revisionNb: number): Observable<Subject> {
        return this.http.get<Subject>(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions/${revisionNb}`);
    }

    findRevisions(login: string, req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/revisions`, {params, observe: 'response'});

    }

    query(req?: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'});
    }

    delete(login: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    findAllByProject(projectName: string, req ?: any): Observable<HttpResponse<Subject[]>> {
        const params = createRequestOption(req);
        return this.http.get<Subject[]>(`${this.projectResourceUrl}/${projectName}/subjects`, {params, observe: 'response'});
    }
}
