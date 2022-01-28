import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class JhiMetricsService {

    constructor(private http: HttpClient) {
    }

    getMetrics(): Observable<HttpResponse<any>> {
        return this.http.get('management/metrics', { observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    threadDump(): Observable<any> {
        return this.http.get('management/dump');
    }
}
