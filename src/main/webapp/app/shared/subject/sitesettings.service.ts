import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {map, mergeMap, shareReplay, tap} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class SiteSettingsService {

    private siteSettingsResourceUrl = 'api/sitesettings';
    private _siteSettings$ = new BehaviorSubject<SiteSettings>(null);
    siteSettings$: Observable<SiteSettings>;

    constructor(private http: HttpClient) {
        this.siteSettings$ = this._siteSettings$.pipe(
            mergeMap(s => s ? of(s) : this.fetchSiteSettings()),
            shareReplay(),
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
            s.hiddenSubjectFields = this.ParseHideableSubjectFields(s.hiddenSubjectFields);
            return s;
        }

        return {
            hiddenSubjectFields: new Set<HideableSubjectField>()
        }
    }

    ParseHideableSubjectFields(fields: Set<string>) : Set<HideableSubjectField> {
        var parsed = new Set<HideableSubjectField>()

        for (let fieldIndex in fields)
            if (this.isHideableSubjectField(fields[fieldIndex]))
                parsed.add(fields[fieldIndex]);
            else
                console.log(`${fields[fieldIndex]} was not recognized as a hideable subject field`)

        return parsed;
    }

    isHideableSubjectField(value: string): value is HideableSubjectField {
        return Object.values(HideableSubjectField).includes(value as HideableSubjectField);
    }
}

export interface SiteSettings {
    hiddenSubjectFields: Set<HideableSubjectField>
}

export enum HideableSubjectField {
    NAME = "person_name",
    DATEOFBIRTH = "date_of_birth",
    GROUP = "group",
}
