import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';

import {JhiLanguageHelper} from '../../shared';
import {PrintService} from "../../shared/util/print.service";

@Component({
    selector: 'jhi-main',
    templateUrl: './main.component.html',
    styleUrls: ['./main.component.scss']
})
export class JhiMainComponent implements OnInit {

    isPrintLocked$ = this.printService.isPrintLocked$;

    constructor(
        private jhiLanguageHelper: JhiLanguageHelper,
        private printService: PrintService,
        private router: Router,
    ) {
    }

    ngOnInit() {
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.jhiLanguageHelper.updateTitle();
            }
        });
    }
}
