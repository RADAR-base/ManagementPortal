import { Component, OnInit } from '@angular/core';
import { ProfileInfo } from './profile-info.model';
import { ProfileService } from './profile.service';

@Component({
    selector: 'jhi-page-ribbon',
    template: `
        <div class="ribbon" *ngIf="ribbonEnv"><a href="" jhiTranslate="global.ribbon.{{ribbonEnv}}">{{ribbonEnv}}</a>
        </div>`,
    styleUrls: [
        'page-ribbon.scss',
    ],
})
export class PageRibbonComponent implements OnInit {

    profileInfo: ProfileInfo;
    ribbonEnv: string;

    constructor(private profileService: ProfileService) {
    }

    ngOnInit() {
        this.profileService.getProfileInfo().then((profileInfo) => {
            this.profileInfo = profileInfo;
            this.ribbonEnv = profileInfo.ribbonEnv;
        });
    }
}
