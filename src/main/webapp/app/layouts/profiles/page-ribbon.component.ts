import { Component, OnInit } from '@angular/core';
import { ProfileInfo } from './profile-info.model';
import { ProfileService } from './profile.service';

@Component({
    selector: 'jhi-page-ribbon',
    template: `
        <div class="ribbon" *ngIf="profileService.profileInfo$ | async as profileInfo">
            <a href="" [translate]="'global.ribbon.' + profileInfo.ribbonEnv"></a>
        </div>`,
    styleUrls: [
        'page-ribbon.scss',
    ],
})
export class PageRibbonComponent {
    constructor(public profileService: ProfileService) {
    }
}
