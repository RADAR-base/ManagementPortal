import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { createRequestOption } from '../../shared/model/request.utils';

@Injectable({ providedIn: 'root' })
export class AuditsService {
    constructor(private http: HttpClient) {
    }

    query(req: any): Observable<HttpResponse<any>> {
        const params = createRequestOption(req);
        return this.http.get('management/audits', {params, observe: 'response'}) as Observable<HttpResponse<any>>;
    }
}
