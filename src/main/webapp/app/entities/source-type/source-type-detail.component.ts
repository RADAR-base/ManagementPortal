import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';

import {SourceType} from './source-type.model';
import {SourceTypeService} from './source-type.service';
import {switchMap} from "rxjs/operators";

@Component({
    selector: 'jhi-source-type-detail',
    templateUrl: './source-type-detail.component.html',
})
export class SourceTypeDetailComponent implements OnInit {
    sourceType$: Observable<SourceType>;

    constructor(
        private sourceTypeService: SourceTypeService,
        private route: ActivatedRoute,
    ) {
    }

    ngOnInit() {
        this.sourceType$ = this.route.params.pipe(
            switchMap((params) => this.sourceTypeService.find(
                params['sourceTypeProducer'], params['sourceTypeModel'], params['catalogVersion'])),
        );
    }

    previousState() {
        window.history.back();
    }
}
