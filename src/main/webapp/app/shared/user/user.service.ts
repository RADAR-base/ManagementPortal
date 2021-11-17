import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import {Observable, of} from 'rxjs';

import { User } from './user.model';
import { createRequestOption } from '../model/request.utils';
import { Project } from '../project';
import {Organization, ORGANIZATIONS} from "../organization/organization.model";

@Injectable({ providedIn: 'root' })
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
        return this.http.get<User>(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    findProject(login: string): Observable<Project[]> {
        return this.http.get<Project[]>(`${this.resourceUrl}/${encodeURIComponent(login)}/projects`);
    }

    findOrganization(login: string): Observable<Organization[]> {
        return of(ORGANIZATIONS);
        // return this.http.get<Organization[]>(`${this.resourceUrl}/${encodeURIComponent(login)}/organizations`);
    }

    query(req?: any): Observable<HttpResponse<User[]>> {
        const params = createRequestOption(req);
        return this.http.get<User[]>(this.resourceUrl, {params, observe: 'response'});
    }

    delete(login: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(login)}`);
    }

    sendActivation(login: string): Observable<any> {
        return this.http.post('api/account/reset-activation/init', login);
    }

    findByProjectAndAuthority(req: any): Observable<User[]> {
        const params = createRequestOption(req);
        return this.http.get<User[]>(this.resourceUrl, {params});
    }
}
