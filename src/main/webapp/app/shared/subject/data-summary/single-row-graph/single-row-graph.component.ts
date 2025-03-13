import { Component, OnInit, Input, AfterViewInit } from '@angular/core';
import Chart from 'chart.js/auto';
import { ChartConfiguration, registerables } from 'chart.js';
@Component({
    selector: 'app-single-row-graph',
    templateUrl: './single-row-graph.component.html',
    styleUrls: ['./../data-summary.component.scss'],
})
export class SingleRowGraphComponent implements OnInit {
    constructor() {}

    @Input() title: string = '';

    @Input() chart: any = {};

    @Input() showTotal: boolean = false;

    @Input() total: Number = 244000;

    @Input() totalValue: Number = 3;

    @Input() totalAverage: Number = 3;

    @Input() isSingleRow: boolean = true;

    @Input() logoClass = '';

    @Input() color: string = '';

    @Input() css: string = '';

    @Input() applyCustomColour = false;

    @Input() longHeading: boolean = false;

    ngAfterViewInit() {
        //@ts-ignore
        // const ctx = document.getElementById(this.charxtId).getContext('2d');

        if (this.applyCustomColour) {
            this.chart.data.datasets[0]['backgroundColor'] = [
                this.color.replace('0.9', '0.6'),
            ];
        }

        this.chartGraph = new Chart(this.chartId, this.chart);
    }
    chartGraph: any = {};
    @Input() chartId: string = '';
    ngOnInit(): void {}
}
