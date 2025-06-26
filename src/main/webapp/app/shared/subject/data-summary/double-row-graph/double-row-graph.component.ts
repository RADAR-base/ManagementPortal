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
      @Input() subtitle1: string = '';
    @Input() title2: string = '';

    @Input() chartId1: string = undefined;
    @Input() chartId2: string = undefined;

    @Input() chartGraph1Data: any = undefined;
    @Input() chartGraph2Data: any = undefined;

    @Input() color: any = '';

    @Input dataPoint1Title: string = '';
    @Input dataPoint2Title: string = '';

    @Input dataPoint1Data: number = 0;
    @Input dataPoint2Data: number = 0;

    @Input dataPoint1SubTitle: string = '';
    @Input dataPoint2SubTitle: string = '';

    @Input css1: string = '';
    @Input css2: string = '';

    @Input addPageBreak: boolean = true;

    chartGraph1: any = {};
    chartGraph2: any = {};

    @Input bothGraphsActive: boolean = true;

    addColourToGraphs() {
        let chartID = this.chartGraph1Data ? this.chartId1 : this.chartId2;
        const ctx = document.getElementById(chartID)?.getContext('2d');

        const gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, this.color);
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0.0)');

        if (this.chartGraph1Data) {
            this.chartGraph1Data.data.datasets[0]['borderColor'] = this.color;
            this.chartGraph1Data.data.datasets[0]['backgroundColor'] = gradient;
        }

        if (this.chartGraph2Data) {
            this.chartGraph2Data.data.datasets[0]['borderColor'] = this.color;
            this.chartGraph2Data.data.datasets[0]['backgroundColor'] = gradient;
        }
    }

    ngOnInit() {
        this.bothGraphsActive = this.chartGraph1Data && this.chartGraph2Data;
    }

    // @ts-nocheck
    ngAfterViewInit() {
        this.bothGraphsActive = this.chartGraph1Data && this.chartGraph2Data;
        this.addColourToGraphs();

        if (this.chartGraph1Data) {
            this.chartGraph1 = new Chart(this.chartId1, this.chartGraph1Data);
        }

        if (this.chartGraph2Data) {
            this.chartGraph2 = new Chart(this.chartId2, this.chartGraph2Data);
        }
    }

    constructor() {}

    ngOnInit(): void {}
}
