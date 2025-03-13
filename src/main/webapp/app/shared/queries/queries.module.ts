import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { queriesRoute } from './queries.route';
import { QueriesComponent } from './queries.component';
import { RouterModule } from '@angular/router';
import { ManagementPortalSharedModule } from '../shared.module';

import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgxAngularQueryBuilderModule } from '@pri17/ngx-angular-query-builder';
import { AddQueryComponent } from './addQuery.component';

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot([...queriesRoute], { useHash: true }),
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        NgxAngularQueryBuilderModule,
    ],
    declarations: [QueriesComponent, AddQueryComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalQueriesModule {}
