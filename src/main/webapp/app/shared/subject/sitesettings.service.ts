import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {SiteSettings} from "./subject.model";
import {HttpClient} from "@angular/common/http";

@Injectable({providedIn: 'root'})
export class SiteSettingsService {

    private siteSettingsResourceUrl = 'api/sitesettings';


    constructor(private http: HttpClient) {
    }

    getSiteSettings(): Observable<SiteSettings> {
        return this.http.get<SiteSettings>(this.siteSettingsResourceUrl);
    }
}
