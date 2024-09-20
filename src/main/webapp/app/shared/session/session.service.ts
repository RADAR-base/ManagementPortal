import {Injectable} from "@angular/core";
import {BehaviorSubject, Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {mergeMap, shareReplay, tap} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class SessionService {

    logoutUrl$: Observable<string>;
    private SessionLogoutResourceUrl = 'api/logout-url';
    private _siteSettings$ = new BehaviorSubject<string>(null);

    constructor(private http: HttpClient) {

        this.logoutUrl$ = this._siteSettings$.pipe(
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
