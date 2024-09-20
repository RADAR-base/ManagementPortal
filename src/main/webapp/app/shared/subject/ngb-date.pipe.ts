/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {Pipe, PipeTransform} from "@angular/core";
import {NgbDateParserFormatter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Pipe({name: 'ngbDate'})
export class NgbDatePipe implements PipeTransform {
    constructor(
        private formatter: NgbDateParserFormatter,
    ) {
    }

    transform(value: NgbDateStruct): string {
        return this.formatter.format(value);
    }
}
