import { Injectable } from '@angular/core';
import { DateUtils } from 'ng-jhipster';
import { Observable } from 'rxjs/Rx';
import { Project } from './project.model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { SourceType } from '../../entities/source-type';
import { createRequestOption } from '../model/request.utils';

@Injectable()
export class ProjectService {

    private resourceUrl = 'api/projects';

    constructor(private http: HttpClient, private dateUtils: DateUtils) {
    }

    create(project: Project): Observable<Project> {
        const copy: Project = Object.assign({}, project);
        copy.startDate = this.dateUtils.toDate(project.startDate);
        copy.endDate = this.dateUtils.toDate(project.endDate);
        return this.http.post(this.resourceUrl, copy) as Observable<Project>;
    }

    update(project: Project): Observable<Project> {
        const copy: Project = Object.assign({}, project);

        copy.startDate = this.dateUtils.toDate(project.startDate);

        copy.endDate = this.dateUtils.toDate(project.endDate);
        return this.http.put(this.resourceUrl, copy) as Observable<Project>;
    }

    find(projectName: string): Observable<Project> {
        return this.http.get(`${this.resourceUrl}/${encodeURIComponent(projectName)}`)
                .map((jsonResponse: any) => {
                    jsonResponse.startDate = this.dateUtils
                    .convertDateTimeFromServer(jsonResponse.startDate);
                    jsonResponse.endDate = this.dateUtils
                    .convertDateTimeFromServer(jsonResponse.endDate);
                    return jsonResponse;
                });
    }

    query(req?: any): Observable<HttpResponse<Project[]>> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, {params: options, observe: 'response'}) as Observable<HttpResponse<Project[]>>;
    }

    findAll(fetchMinimal: boolean): Observable<any> {
        return this.http.get(`${this.resourceUrl}?minimized=${fetchMinimal}`)
        .map((res: any) => this.convertResponseDates(res));
    }

    findSourceTypesByName(projectName: string): Observable<SourceType[]> {
        return this.http.get(`${this.resourceUrl}/${projectName}/source-types`) as Observable<SourceType[]>;
    }

    delete(projectName: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${encodeURIComponent(projectName)}`);
    }

    private convertResponseDates(jsonResponse: any): any {
        for (let i = 0; i < jsonResponse.length; i++) {
            jsonResponse[i].startDate = this.dateUtils
            .convertDateTimeFromServer(jsonResponse[i].startDate);
            jsonResponse[i].endDate = this.dateUtils
            .convertDateTimeFromServer(jsonResponse[i].endDate);
        }
        return jsonResponse;
    }

}
