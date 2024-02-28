import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable, of} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {map, mergeMap, shareReplay, tap} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class SessionService {

    private SessionLogoutResourceUrl = 'api/logout-url';
    private _siteSettings$ = new BehaviorSubject<string>(null);
    logoutUrl$: Observable<string>;

    constructor(private http: HttpClient) {

        this.logoutUrl$ =  this._siteSettings$.pipe(
            mergeMap(s => s ? of(s) : this.fetchLogoutUrl()),
            shareReplay()
        );
    }

    private fetchLogoutUrl() {
        return this.http.get(
            this.SessionLogoutResourceUrl,
            {
                responseType: "text"
            }
        )
            .pipe(
            tap(s => this._siteSettings$.next(s)),
        );
    }
}
