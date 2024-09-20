import {Component} from '@angular/core';
import {ProfileService} from './profile.service';
import {Observable} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {filter, switchMap} from 'rxjs/operators';

@Component({
    selector: 'jhi-page-ribbon',
    template: `
        <div class="ribbon" *ngIf="ribbon$ | async as ribbon">
            <a href="">{{ ribbon }}</a>
        </div>`,
    styleUrls: [
        'page-ribbon.scss',
    ],
})
export class PageRibbonComponent {
    ribbon$: Observable<string | null>

    constructor(
        public profileService: ProfileService,
        translateService: TranslateService,
    ) {
        this.ribbon$ = profileService.profileInfo$.pipe(
            switchMap(profileInfo => {
                const key = 'global.ribbon.' + profileInfo.ribbonEnv
                return translateService.stream(key).pipe(
                    filter(t => t !== key),
                )
            }),
        )
    }
}
