import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable, of} from "rxjs";
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
                hiddenSubjectFields: HideableSubjectField[];
            }
    }

    ParseHideableSubjectFields(fields: string[]) : Set<HideableSubjectField> {
        var parsed = new Set<HideableSubjectField>()

        for (let field in fields)
            if (this.isHideableSubjectField(field))
                parsed.add(field);
            else
                console.log(`${field} was not recognized as a hideable subject field`)

        return parsed;
    }

    isHideableSubjectField(value: string): value is HideableSubjectField {
        return Object.values(HideableSubjectField).includes(value as HideableSubjectField);
    }
}

export interface SiteSettings {
    hiddenSubjectFields: HideableSubjectField[]
}

export enum HideableSubjectField {
    NAME = "person_name",
    DATEOFBIRTH = "date_of_birth",
    GROUP = "group",
}
