import { Component, OnInit } from '@angular/core';

import { Log } from './log.model';
import { LogsService } from './logs.service';

@Component({
    selector: 'jhi-logs',
    templateUrl: './logs.component.html',
})
export class LogsComponent implements OnInit {

    loggers: Log[];
    filter: string;
    orderProp: string;
    reverse: boolean;

    constructor(
            private logsService: LogsService,
    ) {
        this.filter = '';
        this.orderProp = 'name';
        this.reverse = false;
    }

    ngOnInit() {
        this.logsService.findAll().subscribe((loggers) => this.loggers = loggers);
    }

    changeLevel(name: string, level: string) {
        const log = new Log(name, level);
        this.logsService.changeLevel(log).subscribe(() => {
            this.logsService.findAll().subscribe((loggers) => this.loggers = loggers);
        });
    }

    get filteredAndOrderedLoggers() {
        let filtered = !this.filter
            ? this.loggers
            : this.loggers.filter(l =>
                l.name.toLowerCase().includes(this.filter.toLowerCase()));
        return filtered.sort((a, b) => {
            if (a[this.orderProp] < b[this.orderProp]) {
                return this.reverse ? -1 : 1;
            } else if (a[this.orderProp] > b[this.orderProp]) {
                return this.reverse ? 1 : -1;
            }
            return 0;
        });
    }
}
