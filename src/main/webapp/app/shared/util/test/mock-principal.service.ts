import { Observable, ReplaySubject } from "rxjs";
import { Account } from "../../user/account.model";
import { take } from "rxjs/operators";

export class MockPrincipal {
    private _account$ = new ReplaySubject<Account>();
    identity: any;
    fakeResponse: any;
    reset: any;
    account$Spy: any;
    get account$(): Observable<Account | null> {
        return this._account$.asObservable();
    }

    constructor() {
        this.fakeResponse = {};
        this.identity = jasmine.createSpy('identity').and.returnValue(this._account$.toPromise());
        this.reset = jasmine.createSpy('reset').and.returnValue(this._account$.pipe(take(1)));
        this.account$Spy = spyOnProperty(this, 'account$', 'get').and.returnValue(this._account$.asObservable());
    }

    setResponse(json: Account): void {
        this._account$.next(json);
    }
}
