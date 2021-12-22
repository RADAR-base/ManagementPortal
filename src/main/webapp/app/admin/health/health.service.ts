import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Health } from "./health.model";

@Injectable({ providedIn: 'root' })
export class JhiHealthService {

    separator: string;

    constructor(private http: HttpClient) {
        this.separator = '.';
    }

    checkHealth(): Observable<Health> {
        return this.http.get<Health>('management/health');
    }

    getBaseName(name): string {
        if (name) {
            const split = name.split('.');
            return split[0];
        }
    }

    getSubSystemName(name): string {
        if (name) {
            const split = name.split('.');
            split.splice(0, 1);
            const remainder = split.join('.');
            return remainder ? ' - ' + remainder : '';
        }
    }
}
