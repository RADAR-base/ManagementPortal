import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { ManagementPortalAppModule } from './app.module';
import { ProdConfig } from './blocks/config/prod.config';

ProdConfig();

if (module['hot']) {
    module['hot'].accept();
}

platformBrowserDynamic().bootstrapModule(ManagementPortalAppModule);
