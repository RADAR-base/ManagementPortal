import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from './user.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../model/request.utils';
import { Project } from '../project';

@Injectable()
export class UserService {
    private resourceUrl = 'api/users';

    constructor(private http: HttpClient) {
    }

    create(user: User): Observable<any> {
        return this.http.post(this.resourceUrl, user);
    }

    update(user: User): Observable<any> {
        return this.http.put(this.resourceUrl, user);
    }

    find(login: string): Observable<User> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}`) as Observable<User>;
    }

    findProject(login: string): Observable<Project[]> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(login)}/projects`) as Observable<Project[]>;
    }

    query(req?: any): Observable<HttpResponse<User[]>> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params, observe: 'response'}) as Observable<HttpResponse<User[]>>;
    }

    delete(login: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    sendActivation(login: string): Observable<any> {
        return this.http.post('api/account/reset-activation/init', login);
    }

    findByProjectAndAuthority(req: any): Observable<User[]> {
        const params = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params}) as Observable<User[]>;
    }
}
