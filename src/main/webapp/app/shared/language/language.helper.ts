import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import {
    LangChangeEvent,
    TranslateService,
    TranslationChangeEvent,
} from '@ngx-translate/core';

import { LANGUAGES } from './language.constants';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class JhiLanguageHelper {
    readonly languages$: Observable<string[]> = of(LANGUAGES);

    private _titleKey$: BehaviorSubject<string> = new BehaviorSubject<string>('managementPortalApp');
    readonly titleKey$: Observable<string> = this._titleKey$.asObservable();

    constructor(private translateService: TranslateService, private titleService: Title, private router: Router) {
        this.updateTitle();

        combineLatest([
            this.titleKey$,
            this.translateService.onTranslationChange.pipe(startWith(undefined as TranslationChangeEvent)),
            this.translateService.onLangChange.pipe(startWith(undefined as LangChangeEvent)),
        ]).pipe(
            switchMap(([titleKey]) => this.translateService.get(titleKey)),
            map(t => {
                if (typeof t === 'string') {
                    return t;
                } else if (typeof t === 'object') {
                    const values = Object.values(t).filter(v => typeof v === 'string');
                    if (values.length > 0) {
                        return values[0] as string;
                    }
                }
                return 'ManagementPortal';
            }),
            catchError(() => of('ManagementPortal')),
        ).subscribe(title => {
            this.titleService.setTitle(title);
        });
    }

    /**
     * Update the window title using params in the following
     * order:
     * 1. titleKey parameter
     * 2. $state.$current.data.pageTitle (current state page title)
     * 3. 'global.title'
     */
    updateTitle(titleKey?: string) {
        this._titleKey$.next(titleKey || this.getPageTitle(this.router.routerState.snapshot.root));
    }

    private getPageTitle(routeSnapshot: ActivatedRouteSnapshot) {
        let title: string = (routeSnapshot.data && routeSnapshot.data['pageTitle']) ? routeSnapshot.data['pageTitle'] : 'managementPortalApp';
        if (routeSnapshot.firstChild) {
            title = this.getPageTitle(routeSnapshot.firstChild) || title;
        }
        return title;
    }
}
