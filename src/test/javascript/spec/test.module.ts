import { NgModule } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { MockLanguageService } from './helpers/mock-language.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DatePipe } from '@angular/common';

@NgModule({
    providers: [
        DatePipe,
        {
            provide: JhiLanguageService,
            useClass: MockLanguageService
        }

    ],
    imports: [HttpClientTestingModule]
})
export class ManagementPortalTestModule {}
