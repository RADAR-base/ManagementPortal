import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { first, map, switchMap, tap } from 'rxjs/operators';

import { ProfileInfo } from './profile-info.model';
import { BehaviorSubject, Observable, of } from 'rxjs';

interface MinimalProfileInfo {
    activeProfiles: string[],
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
    private profileInfoUrl = 'api/profile-info';
    private _activeProfiles$: BehaviorSubject<MinimalProfileInfo | null> = new BehaviorSubject(null);
    profileInfo$: Observable<ProfileInfo>

    constructor(private http: HttpClient) {
        this.profileInfo$ = this._activeProfiles$.pipe(
            first(),
            switchMap(p => {
                if (!p) {
                    return this.http.get<MinimalProfileInfo>(this.profileInfoUrl).pipe(
                        tap(p => this._activeProfiles$.next(p)),
                    )
                } else {
                    return of(p);
                }
            }),
            map(p => ({
                activeProfiles: p.activeProfiles,
                ribbonEnv: p.activeProfiles.includes('dev') ? 'dev' : '',
                inProduction: p.activeProfiles.includes('prod'),
                apiDocsEnabled: p.activeProfiles.includes('api-docs'),
            })),
        );
    }
}
