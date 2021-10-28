import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';

import { ProfileInfo } from './profile-info.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {

    private profileInfoUrl = 'api/profile-info';
    private profileInfo: Promise<ProfileInfo>;

    constructor(private http: HttpClient) {
    }

    getProfileInfo(): Promise<ProfileInfo> {
        if (!this.profileInfo) {
            this.profileInfo = this.http.get<ProfileInfo>(this.profileInfoUrl)
                .pipe(map(data => {
                    const pi = new ProfileInfo();
                    pi.activeProfiles = data.activeProfiles;
                    pi.ribbonEnv = data.activeProfiles.includes('dev') ? 'dev' : ''
                    pi.inProduction = data.activeProfiles.includes('prod');
                    pi.apiDocsEnabled = data.activeProfiles.includes('api-docs');
                    return pi;
                })).toPromise();
        }
        return this.profileInfo;
    }
}
