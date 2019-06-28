import { Injectable } from '@angular/core';
import { ProfileInfo } from './profile-info.model';
import {HttpClient, HttpResponse} from '@angular/common/http';

@Injectable()
export class ProfileService {

    private profileInfoUrl = 'api/profile-info';
    private profileInfo: Promise<ProfileInfo>;

    constructor(private http: HttpClient) {
    }

    getProfileInfo(): Promise<ProfileInfo> {
        if (!this.profileInfo) {
            this.profileInfo = this.http.get<ProfileInfo>(this.profileInfoUrl, { observe: 'response' })
                    .map((res: HttpResponse<ProfileInfo>) => {
                        const data = res.body;
                        const pi = new ProfileInfo();
                        pi.activeProfiles = data.activeProfiles;
                        pi.ribbonEnv = data.ribbonEnv;
                        pi.inProduction = data.activeProfiles.indexOf('prod') !== -1;
                        pi.swaggerEnabled = data.activeProfiles.indexOf('swagger')!== -1;
                        return pi;
                    }).toPromise();
        }
        return this.profileInfo;
    }
}
