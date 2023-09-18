import { Injectable } from '@angular/core';
import { BehaviorSubject } from "rxjs";

@Injectable({ providedIn: 'root' })
export class PrintService {

    isPrintLocked$:  BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    setPrintLockTo(setTo: boolean) {
        setTimeout(() => {
            this.isPrintLocked$.next(setTo)
        });
    }
}
