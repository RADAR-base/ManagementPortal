import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Log } from './log.model';

@Injectable({ providedIn: 'root' })
export class LogsService {
    constructor(private http: HttpClient) {
    }

    changeLevel(log: Log): Observable<HttpResponse<any>> {
        return this.http.put('management/logs', log, { observe: 'response'}) as Observable<HttpResponse<any>>;
    }

    findAll(): Observable<Log[]> {
        return this.http.get<Log[]>('management/logs');
    }
}
