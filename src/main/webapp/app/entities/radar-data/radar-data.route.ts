import { Routes } from '@angular/router';
import { RadarDataComponent } from "./radar-data.component";

export const radarDataRoute: Routes = [
    {
            path: 'radarData',
            component: RadarDataComponent,
            data: {
                authorities: [],
                pageTitle: 'global.menu.entities.RadarData',
            },
        },
]