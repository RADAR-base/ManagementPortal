import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable, of} from "rxjs";
import {SiteSettings} from "./subject.model";
import {HttpClient} from "@angular/common/http";
import {map, share, switchMap, tap} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class SiteSettingsService {

    private siteSettingsResourceUrl = 'api/sitesettings';
    private _siteSettings$ = new BehaviorSubject<SiteSettings>(null);
    siteSettings$ = new Observable<SiteSettings>(null);

    constructor(private http: HttpClient) {
        this.siteSettings$ = this._siteSettings$.pipe(
            switchMap(s => s ? of(s) : this.fetchSiteSettings()),
            share(),
        );
    }

    private fetchSiteSettings() {
        return this.http.get<SiteSettings>(this.siteSettingsResourceUrl).pipe(
            map(s => this.checkAndClean(s)),
            tap(s => this._siteSettings$.next(s)),
        );
    }

    private checkAndClean(s: SiteSettings) : SiteSettings {
        if (s != null &&
            typeof s == 'object' &&
            Array.isArray(s.hiddenSubjectFields)
        ){
            return s;
        }
        else
            return new class implements SiteSettings {
                hiddenSubjectFields: string[];
            }
    }
}
