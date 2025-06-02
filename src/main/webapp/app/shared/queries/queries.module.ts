import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { queriesRoute } from './queries.route';
import { QueryGroupListComponent } from './queryGroupList.component';
import { RouterModule } from '@angular/router';
import { ManagementPortalSharedModule } from '../shared.module';

import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgxAngularQueryBuilderModule } from "@uom-digital-health-software/ngx-angular-query-builder";
import { AddQueryComponent } from './addQuery.component';
import { ContentItemComponent } from './content/content-item/content-item.component';
import { ContentComponent } from './content/content.component';
import { VideoItemComponent } from './content/video-item/video-item.component';
import { ParagraphItemComponent } from './content/paragraph-item/paragraph-item.component';
import { ImageItemComponent } from './content/image-item/image-item.component';
import { EditorModule } from '@tinymce/tinymce-angular';
import { ModalContentComponent } from './content/modal-content/modal-content.component';

@NgModule({
    imports: [
        ManagementPortalSharedModule,
        RouterModule.forRoot([...queriesRoute], { useHash: true }),
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        NgxAngularQueryBuilderModule,
        EditorModule

    ],
    declarations: [QueryGroupListComponent,AddQueryComponent, ContentItemComponent, ContentComponent, VideoItemComponent, ParagraphItemComponent, ImageItemComponent, ModalContentComponent],
    entryComponents: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ManagementPortalQueriesModule {}
