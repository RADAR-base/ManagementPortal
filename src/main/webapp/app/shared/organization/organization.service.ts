import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';

import {Organization, ORGANIZATIONS} from './organization.model';
import {Project} from "../project";
import {convertDateTimeFromServer, toDate} from "../util/date-util";
import {map} from "rxjs/operators";
import {createRequestOption} from "../model/request.utils";
import {SourceType} from "../../entities/source-type";

@Injectable({ providedIn: 'root' })
export class OrganizationService {

    private resourceUrl = 'api/organizations';

    constructor(private http: HttpClient) {
    }

    create(organization: Organization): Observable<Organization> {
        return this.http.post<Organization>(this.resourceUrl, organization);
    }

    // create(project: Project): Observable<Project> {
    //     const copy: Project = Object.assign({}, project);
    //     copy.startDate = toDate(project.startDate);
    //     copy.endDate = toDate(project.endDate);
    //     return this.http.post<Project>(this.resourceUrl, copy);
    // }

    find(organizationName: string): Observable<Organization> {
        console.log(organizationName)
        const organization = ORGANIZATIONS.filter(org => org.organizationName === organizationName)[0];
        if(organization){
            return of(organization);
        } else {
            return of(null)
        }
        // return this.http.get(`${this.resourceUrl}/${encodeURIComponent(organizationName)}`);
    }

    // find(projectName: string): Observable<Project> {
    //     return this.http.get(`${this.resourceUrl}/${encodeURIComponent(projectName)}`)
    //             .pipe(map((jsonResponse: any) => {
    //                 jsonResponse.startDate = convertDateTimeFromServer(jsonResponse.startDate);
    //                 jsonResponse.endDate = convertDateTimeFromServer(jsonResponse.endDate);
    //                 return jsonResponse;
    //             }));
    // }

    findAll(): Observable<Organization[]> {
        return of(ORGANIZATIONS);
        // return this.http.get<Organization[]>(this.resourceUrl);
    }

    // findAll(fetchMinimal: boolean): Observable<any> {
    //     return this.http.get(`${this.resourceUrl}?minimized=${fetchMinimal}`)
    //             .pipe(map((res: any) => this.convertResponseDates(res)));
    // }



    update(project: Project): Observable<Organization> {
        const copy: Project = Object.assign({}, project);
        copy.startDate = toDate(project.startDate);
        copy.endDate = toDate(project.endDate);
        return this.http.put<Project>(this.resourceUrl, copy);
    }



    // query(req?: any): Observable<HttpResponse<Organization[]>> {
    //     console.log('query')
    //     const options = createRequestOption(req);
    //     return of(new HttpResponse(ORGANIZATIONS)); //of(ORGANIZATIONS);
    //     // return this.http.get<Project[]>(this.resourceUrl, {params: options, observe: 'response'});
    // }



    // findSourceTypesByName(projectName: string): Observable<SourceType[]> {
    //     return this.http.get<SourceType[]>(`${this.resourceUrl}/${projectName}/source-types`);
    // }

    delete(organizationName: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(organizationName)}`);
    }

    // private convertResponseDates(jsonResponse: any): any {
    //     for (let i = 0; i < jsonResponse.length; i++) {
    //         jsonResponse[i].startDate = convertDateTimeFromServer(jsonResponse[i].startDate);
    //         jsonResponse[i].endDate = convertDateTimeFromServer(jsonResponse[i].endDate);
    //     }
    //     return jsonResponse;
    // }
}
