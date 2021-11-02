import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { Project } from './project.model';
import { SourceType } from '../../entities/source-type';
import { createRequestOption } from '../model/request.utils';
import { convertDateTimeFromServer, toDate } from '../util/date-util';

@Injectable({ providedIn: 'root' })
export class ProjectService {

    private resourceUrl = 'api/projects';

    constructor(private http: HttpClient) {
    }

    create(project: Project): Observable<Project> {
        const copy: Project = Object.assign({}, project);
        copy.startDate = toDate(project.startDate);
        copy.endDate = toDate(project.endDate);
        return this.http.post<Project>(this.resourceUrl, copy);
    }

    update(project: Project): Observable<Project> {
        const copy: Project = Object.assign({}, project);
        copy.startDate = toDate(project.startDate);
        copy.endDate = toDate(project.endDate);
        return this.http.put<Project>(this.resourceUrl, copy);
    }

    find(projectName: string): Observable<Project> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(projectName)}`)
            .pipe(map((jsonResponse: any) => {
                jsonResponse.startDate = convertDateTimeFromServer(jsonResponse.startDate);
                jsonResponse.endDate = convertDateTimeFromServer(jsonResponse.endDate);
                return jsonResponse;
            }));
    }

    query(req?: any): Observable<HttpResponse<Project[]>> {
        const options = createRequestOption(req);
        return this.http.get<Project[]>(this.resourceUrl, {params: options, observe: 'response'});
    }

    findAll(fetchMinimal: boolean): Observable<any> {
        return this.http.get(`${this.resourceUrl}?minimized=${fetchMinimal}`)
            .pipe(map((res: any) => this.convertResponseDates(res)));
    }

    findSourceTypesByName(projectName: string): Observable<SourceType[]> {
        return this.http.get<SourceType[]>(`${this.resourceUrl}/${projectName}/source-types`);
    }

    delete(projectName: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(projectName)}`);
    }

    private convertResponseDates(jsonResponse: any): any {
        for (let i = 0; i < jsonResponse.length; i++) {
            jsonResponse[i].startDate = convertDateTimeFromServer(jsonResponse[i].startDate);
            jsonResponse[i].endDate = convertDateTimeFromServer(jsonResponse[i].endDate);
        }
        return jsonResponse;
    }
}
