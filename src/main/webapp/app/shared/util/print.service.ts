import { Injectable } from '@angular/core';
import { BehaviorSubject } from "rxjs";

@Injectable({ providedIn: 'root' })
export class PrintService {

    public isPrintLocked$:  BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    constructor(
    ) {
        this.isPrintLocked$.subscribe(isPrintLocked => {
            //console.log(`isPrintlocked: ${isPrintLocked}`)
        })
    }

    setPrintLockTo(setTo: boolean) {
        this.isPrintLocked$.next(setTo)
    }
}
