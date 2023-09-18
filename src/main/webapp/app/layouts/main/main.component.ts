import { Component, OnInit } from '@angular/core';
import { ActivatedRouteSnapshot, NavigationEnd, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { JhiLanguageHelper } from '../../shared';
import {PrintService} from "../../shared/util/print.service";

@Component({
    selector: 'jhi-main',
    templateUrl: './main.component.html',
    styleUrls: ['./main.component.scss']
})
export class JhiMainComponent implements OnInit {

    constructor(
            private jhiLanguageHelper: JhiLanguageHelper,
            private printService: PrintService,
            private router: Router,
    ) {
    }

    public isPrintLocked$: Observable<boolean>;

    ngOnInit() {
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.jhiLanguageHelper.updateTitle();
            }
        });
       this.isPrintLocked$ = this.printService.isPrintLocked$;
    }
}
