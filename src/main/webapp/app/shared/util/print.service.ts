import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {delay} from "rxjs/operators";

@Injectable({ providedIn: 'root' })
export class PrintService {

    private _isPrintLocked$:  BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    isPrintLocked$:  Observable<boolean> = this._isPrintLocked$.pipe(
        // this delay is needed to propagate the style changes upward to main.component.ts
        delay(0),
    );

    setPrintLockTo(setTo: boolean) {
            this._isPrintLocked$.next(setTo);
    }
}
