import { Component, Inject, SecurityContext } from '@angular/core';
import { APP_BASE_HREF } from '@angular/common';
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";

@Component({
    selector: 'jhi-docs',
    templateUrl: './docs.component.html',
})
export class JhiDocsComponent {
    apiDocsUrl: SafeUrl;

    constructor(
      @Inject(APP_BASE_HREF) private baseHref: string,
      private sanitizer: DomSanitizer
    ) {
        this.apiDocsUrl = sanitizer.bypassSecurityTrustResourceUrl(
          'swagger-ui/index.html?configUrl=' + baseHref + 'api-docs/swagger-config');
    }
}
