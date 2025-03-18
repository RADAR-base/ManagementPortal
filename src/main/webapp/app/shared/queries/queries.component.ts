import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'jhi-queries',
    templateUrl: './queries.component.html',
})
export class QueriesComponent implements OnInit, OnDestroy {
    constructor(private httpclient: HttpClient) {}
    public queryList: any;

    private baseUrl = 'api/query-builder';

    ngOnInit() {
        this.httpclient.get(this.baseUrl + '/queries').subscribe((result) => {
            this.queryList = result;
            console.log(result);
        });
    }

    ngOnDestroy() {}
}
