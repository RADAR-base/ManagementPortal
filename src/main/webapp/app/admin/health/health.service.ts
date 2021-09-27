import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class JhiHealthService {

    separator: string;

    constructor(private http: HttpClient) {
        this.separator = '.';
    }

    checkHealth(): Observable<any> {
        return this.http.get('management/health');
    }

    transformHealthData(data): any {
        const response = [];
        this.flattenHealthData(response, null, data);
        return response;
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

    /* private methods */
    private addHealthObject(result, isLeaf, healthObject, name): any {
        const healthData: any = {
            name,
        };
        if (healthObject.details) {
            healthData.details = healthObject.details;
        }
        if (healthObject.status) {
            healthData.status = healthObject.status;
        }
        if (healthObject.error) {
            healthData.error = healthObject.error;
        }
        let hasDetails = !!healthData.details;

        // Only add nodes if they provide additional information
        if (isLeaf || hasDetails || healthData.error) {
            result.push(healthData);
        }
        return healthData;
    }

    private flattenHealthData(result, path, data): any {
        for (const [key, value] of Object.entries(data.components || {})) {
            if (this.isHealthObject(value)) {
                if (this.hasSubSystem(value)) {
                    this.addHealthObject(result, false, value, this.getModuleName(path, key));
                    this.flattenHealthData(result, this.getModuleName(path, key), value);
                } else {
                    this.addHealthObject(result, true, value, this.getModuleName(path, key));
                }
            }
        }
        return result;
    }

    private getModuleName(path, name): string {
        let result;
        if (path && name) {
            result = path + this.separator + name;
        } else if (path) {
            result = path;
        } else if (name) {
            result = name;
        } else {
            result = '';
        }
        return result;
    }

    private hasSubSystem(healthObject): boolean {
        return 'components' in healthObject;
    }

    private isHealthObject(healthObject): boolean {
        let result = false;

        for (const key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                if (key === 'status') {
                    result = true;
                }
            }
        }
        return result;
    }
}
