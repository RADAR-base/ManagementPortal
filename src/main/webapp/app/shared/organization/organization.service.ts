import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {BehaviorSubject, combineLatest, Observable, of, Subject, throwError} from 'rxjs';

import {Organization} from './organization.model';
import {Principal} from '../auth/principal.service';
import {AlertService} from '../util/alert.service';
import {
    concatMap,
    delay,
    distinctUntilChanged,
    filter,
    map,
    retryWhen,
    startWith,
    switchMap,
    tap
} from 'rxjs/operators';
import {createRequestOption} from '../model/request.utils';

@Injectable({providedIn: 'root'})
export class OrganizationService {
    private readonly _organizations$ = new BehaviorSubject<Organization[]>([]);
    organizations$: Observable<Organization[]> = this._organizations$.asObservable();
    private readonly _trigger$ = new Subject<void>();
    private resourceUrl = 'api/organizations';

    constructor(
        private http: HttpClient,
        private principal: Principal,
        private alertService: AlertService,
    ) {
        combineLatest([
            principal.account$,
            this._trigger$.pipe(startWith(undefined as void)),
        ]).pipe(
            switchMap(([account]) => {
                if (account) {
                    return this.fetch().pipe(
                        retryWhen(errors => errors.pipe(
                            concatMap((error, count) => {
                                if (count <= 10 && (!error.status || error.status >= 500)) {
                                    return of(error);
                                }
                                return throwError(error);
                            }),
                            delay(1000),
                        )),
                    );
                } else {
                    return of([]);
                }
            }),
            distinctUntilChanged((a, b) => a === b || (a && b && JSON.stringify(a) === JSON.stringify(b))),
        ).subscribe(
            organizations => this._organizations$.next(organizations),
            err => this.alertService.error(err.message, null, null),
        );
    }

    reset() {
        this._trigger$.next();
    }

    create(organization: Organization): Observable<Organization> {
        return this.http.post(this.organizationUrl(), organization).pipe(
            tap(
                o => this.updateOrganization({
                    projects: [],
                    ...o,
                }),
                () => this.reset(),
            ),
        );
    }

    update(organization: Organization): Observable<Organization> {
        return this.http.put<Organization>(this.organizationUrl(), organization).pipe(
            tap(
                o => this.updateOrganization(o),
                () => this.reset(),
            ),
        );
    }

    find(orgName: string): Observable<Organization> {
        return this.organizations$.pipe(
            map(organizations => organizations.find(o => o.name === orgName)),
            filter(o => !!o),
            distinctUntilChanged((a, b) => a === b || JSON.stringify(a) === JSON.stringify(b)),
        );
    }

    fetch(): Observable<Organization[]> {
        return this.query().pipe(
            map(res => res.body),
        );
    }

    query(req?: any): Observable<HttpResponse<Organization[]>> {
        const options = createRequestOption(req);
        return this.http.get<Organization[]>(this.organizationUrl(), {params: options, observe: 'response'});
    }

    delete(orgName: string): Observable<any> {
        return this.http.delete(this.organizationUrl(orgName)).pipe(
            tap(
                () => {
                    const newOrganizations = this._organizations$.value.slice()
                    const idx = newOrganizations.findIndex(p => p.name === orgName);
                    if (idx >= 0) {
                        newOrganizations.splice(idx, 1);
                        this._organizations$.next(newOrganizations);
                    }
                },
                () => this.reset(),
            ),
        );
    }

    findAll(): Observable<Organization[]> {
        return this.http.get<Organization[]>(this.resourceUrl);
    }

    protected organizationUrl(orgName?: string): string {
        if (orgName) {
            return this.resourceUrl + '/' + encodeURIComponent(orgName);
        } else {
            return this.resourceUrl;
        }
    }

    private updateOrganization(organization: Organization) {
        const nextValue = this._organizations$.value.slice();
        const idx = nextValue.findIndex(p => p.id === organization.id);
        let needsAuthRenewal = false;
        if (idx >= 0) {
            if (nextValue[idx].name !== organization.name) {
                needsAuthRenewal = true;
            }
            nextValue[idx] = {
                ...nextValue[idx],
                ...organization,
            };
        } else {
            nextValue.push(organization);
        }
        this._organizations$.next(nextValue);
        if (needsAuthRenewal) {
            this.principal.reset();
        }
    }
}
