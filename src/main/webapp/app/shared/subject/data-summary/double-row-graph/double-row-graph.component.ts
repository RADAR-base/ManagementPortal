// @ts-nocheck
import { Component, OnInit, Input } from '@angular/core';
import Chart from 'chart.js/auto';

@Component({
    selector: 'app-double-row-graph',
    templateUrl: './double-row-graph.component.html',
    styleUrls: ['./../data-summary.component.scss'],
})
export class DoubleRowGraphComponent implements OnInit {
    @Input() title1: string = '';

    @Input() title2: string = '';

    @Input() chartId1: string = 'test';

    @Input() chartId2: string = 'test1';

    @Input() chartGraph1Data: any = '';

    @Input() chartGraph2Data: any = '';

    @Input() color: any = '';

    @Input dataPoint1Title: string = '';
    @Input dataPoint2Title: string = '';

    @Input dataPoint1Data: number = 0;
    @Input dataPoint2Data: number = 0;

    @Input dataPoint1SubTitle: string = '';
    @Input dataPoint2SubTitle: string = '';

    @Input css1: string = '';
    @Input css2: string = '';

    chartGraph1: any = {};
    chartGraph2: any = {};

    addColourToGraphs() {
        const ctx = document.getElementById(this.chartId1)?.getContext('2d');

        const gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, this.color);
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0.0)');

        this.chartGraph1Data.data.datasets[0]['borderColor'] = this.color;
        this.chartGraph2Data.data.datasets[0]['borderColor'] = this.color;

        this.chartGraph1Data.data.datasets[0]['backgroundColor'] = gradient;
        this.chartGraph2Data.data.datasets[0]['backgroundColor'] = gradient;
    }
    // @ts-nocheck
    ngAfterViewInit() {
        this.addColourToGraphs();
        this.chartGraph1 = new Chart(this.chartId1, this.chartGraph1Data);
        this.chartGraph2 = new Chart(this.chartId2, this.chartGraph2Data);
    }

    constructor() {}

    ngOnInit(): void {}
}
