import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';
import { Log } from './log.model';

@Injectable()
export class LogsService {
    constructor(private http: HttpClient) {
    }

    changeLevel(log: Log): Observable<HttpResponse<any>> {
        return this.http.put('management/logs', log, { observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    findAll(): Observable<Log[]> {
        return this.http.get('management/logs', { observe: 'response'}).map((res: HttpResponse<any>) => res.body);
    }
}
