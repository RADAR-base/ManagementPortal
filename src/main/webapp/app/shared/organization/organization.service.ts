import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Organization } from './organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {

    private resourceUrl = 'api/organizations';

    constructor(private http: HttpClient) {
    }

    create(organization: Organization): Observable<Organization> {
        return this.http.post<Organization>(this.resourceUrl, organization);
    }

    find(orgName: string): Observable<Organization> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(orgName)}`);
    }

    findAll(): Observable<Organization[]> {
        return this.http.get<Organization[]>(this.resourceUrl);
    }
}
