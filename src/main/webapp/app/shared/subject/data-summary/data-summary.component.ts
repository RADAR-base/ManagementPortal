import { Component, OnInit } from '@angular/core';

import Chart from 'chart.js/auto';
import { ChartConfiguration, registerables } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
Chart.register(...registerables, ChartDataLabels);
import { Graph } from './types';
import { SubjectService } from '../subject.service';
import { ActivatedRoute } from '@angular/router';
import { Subject } from '../subject.model';
import { HttpResponse } from '@angular/common/http';

const domainGraph: Graph = {
    type: 'line',
    showScaleY: false,
    showDataTables: false,
};

const barGraph: Graph = {
    type: 'bar',
    showScaleY: false,
    showDataTables: true,
};

const lineGraph: Graph = {
    type: 'line',
    showScaleY: false,
    showDataTables: true,
};

const histogramGraph: Graph = {
    type: 'histogram',
    showScaleY: false,
    showDataTables: true,
};
const socialMap = {
    '1': 'On my own',
    '2': 'With strangers',
    '3': 'With people I know',
    '4': 'With people I am close to',
    '5': 'Connecting with people online or using social media',
};

@Component({
    selector: 'app-data-summary',
    templateUrl: './data-summary.component.html',
    styleUrls: ['./data-summary.component.scss'],
})
export class DataSummaryComponent implements OnInit {
    title = 'ng-chart';
    chart: any = [];
    chart_heart_rate: any = [];
    chart_heart_rate_variability: any = [];
    questionnaireDomains: any = [''];
    chart_type: { [id: string]: Graph } = {
        delusion: domainGraph,
        negative_emotions: domainGraph,
        positive_emotions: domainGraph,
        dissociation: domainGraph,
        stress: domainGraph,
        sleep_period: domainGraph,
        hope: domainGraph,
        mood: domainGraph,
        anxiety: domainGraph,
        self_esteem: domainGraph,
        connectedness: domainGraph,
        coping: domainGraph,
        fear: domainGraph,
        hallucination_hear: domainGraph,
        hallucination_vision: domainGraph,
        threat: domainGraph,

        questionnaire: barGraph,
        heart_rate: lineGraph,
        hrv: lineGraph,

        steps: barGraph,
        activity: lineGraph,
        respiratory_rate: lineGraph,
        screen_usage: lineGraph,

        social: histogramGraph,
        social_1: barGraph,
        social_2: barGraph,
        social_3: barGraph,
        social_4: barGraph,
        social_5: barGraph,

        sleep: histogramGraph,
        sleep_1: barGraph,
        sleep_2: barGraph,
        sleep_3: barGraph,
        sleep_4: barGraph,
        sleep_5: barGraph,
        sleep_6: barGraph,
        sleep_7: barGraph,
        sleep_8: barGraph,

        wherearebout: histogramGraph,
        wherearebout_1: barGraph,
        wherearebout_2: barGraph,
        wherearebout_3: barGraph,
        wherearebout_4: barGraph,
        wherearebout_5: barGraph,
        wherearebout_6: barGraph,
        wherearebout_7: barGraph,
        wherearebout_8: barGraph,
        wherearebout_9: barGraph,
        wherearebout_10: barGraph,
        wherearebout_11: barGraph,
    };
    data: any = {};
    monthLabelsPerGraph: any = {};
    charts: any = {};
    monthLabels: string[] = [];

    questionnaireTotal: number = 0;
    questionnaireAverage: number = 0;

    stepsTotal: number = 0;
    stepsAverage: number = 0;

    questionnaireKey: string = 'questionnaire';
    socialMap = {
        '1': 'On my own',
        '2': 'With strangers',
        '3': 'With people I know',
        '4': 'With people I am close to',
        '5': 'Connecting with people online or using social media',
    };

    socialLabels = [
        'On my own',
        'With strangers',
        'With people I know',
        'With people I am close to',
        'Connecting with people online or using social media',
    ];
    socialKeys = Object.keys(this.socialMap);

    sleepMap = {
        '0-2': '0-2 hrs',
        '2-4': '2-4 hrs',
        '4-6': '4-6 hrs',
        '6-8': '6-8 hrs',
        '8-10': '8-10 hrs',
        '10-12': '10-12 hrs',
        '12-14': '12-14 hrs',
        '14+': '14+ hrs+',
    };

    sleepLabels = [
        '0-2 hrs',
        '2-4 hrs',
        '4-6 hrs',
        '6-8 hrs',
        '8-10 hrs',
        '10-12 hrs',
        '12-14 hrs',
        '14+',
    ];

    sleepMapKeys = Object.keys(this.sleepMap);

    whereaboutsMap = {
        '1': 'Relaxing (e.g. watching TV, reading a book, resting, other)',
        '2': 'Working / at School',
        '3': 'Studying',
        '4': 'Housekeeping',
        '5': 'Shopping',
        '6': 'Hygiene / Self-care activity',
        '7': 'Eating/Drinking',
        '8': 'Travelling',
        '9': 'Exercising (e.g. walking, jogging, dancing, playing sport, other)',
        '10': 'Leisure Activity (e.g. going to an event, visiting a museum, cinema or library, etc)',
        '11': 'Nothing',
    };

    whereaboutsLabels = [
        'Relaxing (e.g. watching TV, reading a book, resting, other)',
        'Working / at School',
        'Studying',
        'Housekeeping',
        'Shopping',
        'Hygiene / Self-care activity',
        'Eating/Drinking',
        'Travelling',
        'Exercising (e.g. walking, jogging, dancing, playing sport, other)',
        'Leisure Activity (e.g. going to an event, visiting a museum, cinema or library, etc)',
        'Nothing',
    ];

    whereaboutsMapKeys = Object.keys(this.whereaboutsMap);

    subject: Subject;
    private subscription: any;

    constructor(
        private subjectService: SubjectService,
        private route: ActivatedRoute
    ) {}

    createMonthHistogram(key: string, months: string[], binLabels2: string[]) {
        const binColors = [
            'rgba(255, 99, 132, 0.6)',
            'rgba(54, 162, 235, 0.6)',
            'rgba(255, 206, 86, 0.6)',
            'rgba(75, 192, 192, 0.6)',
            'rgba(153, 102, 255, 0.6)',
            'rgba(255, 159, 64, 0.6)',
            'rgba(0, 204, 102, 0.6)',
            'rgba(204, 0, 204, 0.6)',
            'rgba(102, 102, 102, 0.6)',
            'rgba(0, 153, 255, 0.6)',
            'rgba(255, 51, 153, 0.6)',
        ];

        let activeColors = binColors.slice(0, binLabels2.length);

        const data = this.data[key];

        return {
            type: 'bar',
            data: {
                labels: months,
                datasets: binLabels2.map((label, index) => ({
                    label: label,
                    data: data[index],
                    backgroundColor: activeColors[index],
                    borderColor: activeColors[index].replace('0.6', '1'),
                    borderWidth: 1,
                })),
            },
            options: {
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                },
                plugins: {
                    datalabels: {
                        display: false,
                    },
                },
            },
        };
    }

    createLineChart(
        id: string,
        labels: string[],
        data: number[],
        showScaleY: boolean,
        showDataLables: boolean,
        color: string = 'rgba(110, 37, 147, 0.9)'
    ) {
        let graphObject = {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        fill: true,
                        tension: 0.2,
                        data: data,
                        borderWidth: 2,
                        borderColor: color,
                    },
                ],
            },
            options: {
                devicePixelRatio: 4,
                layout: {
                    padding: {
                        left: 30,
                        right: 30,
                        top: 30,
                        bottom: 30,
                    },
                },
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        display: showScaleY,
                    },
                },
                plugins: {
                    title: {
                        display: false,
                    },
                    legend: {
                        display: false,
                        position: 'top',
                    },
                    datalabels: {
                        display: showDataLables,
                        align: 'top',
                        formatter: (value) => value,
                        color: '#000',
                        font: {
                            weight: 'bold',
                            size: 12,
                        },
                    },
                },
            },
        };

        if (showDataLables) {
            graphObject['options']['datalabels'] = {
                align: 'top',

                formatter: (value) => value,
                color: '#000',
                font: {
                    weight: 'bold',
                    size: 12,
                },
            };
        } else {
            graphObject['options']['datalabels'] = {
                display: false,
            };
        }

        return graphObject;
    }

    createBarChart(
        id: string,
        labels: string[],
        data: number[],
        barColor: string = 'rgba(191,	236,	235	, 1)'
    ) {
        return {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        data: data,
                        borderWidth: 1,
                        borderRadius: 5,
                        barThickness: 30,
                        backgroundColor: [barColor],
                    },
                ],
            },
            options: {
                devicePixelRatio: 4,
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        display: false,
                    },
                },
                plugins: {
                    title: {
                        display: false,
                    },
                    legend: {
                        display: false,
                        position: 'top',
                    },
                    datalabels: {
                        anchor: 'end',

                        formatter: (value) => value,
                        color: '#000',
                        font: {
                            weight: 'bold',
                            size: 8,
                        },
                        offset: -20,
                        align: 'top',
                    },
                },
            },
        };
    }

    formatMonth(monthString: string) {
        const [year, month] = monthString.split('-').map(Number);
        const date = new Date(year, month - 1);

        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
        }).format(date);
    }

    // this only applies to Questionnaires and Steps
    calculateTotalsAndAverage() {
        let questionnaireData = this.data[this.questionnaireKey];

        if (questionnaireData && questionnaireData.length > 0) {
            this.questionnaireTotal = questionnaireData.reduce(
                (acc, num) => acc + num,
                0
            );

            this.questionnaireAverage =
                this.questionnaireTotal / questionnaireData.length;

            this.questionnaireAverage = Number(
                this.questionnaireAverage.toFixed(1)
            );
        }

        let stepsData = this.data['steps'];

        if (stepsData && stepsData.length > 0) {
            this.stepsTotal = stepsData.reduce((acc, num) => acc + num, 0);

            this.stepsAverage = this.stepsTotal / stepsData.length;

            this.stepsAverage = Number(this.stepsAverage.toFixed(1));
        }
    }

    addMonthPerKey(key: string, month: string) {
        if (this.monthLabelsPerGraph[key] == undefined) {
            this.monthLabelsPerGraph[key] = [];
        }
        this.monthLabelsPerGraph[key].push(this.formatMonth(month));
    }
    pushToData(key: string, value: number | string) {
        if (this.data[key] == undefined) {
            this.data[key] = [];
        }
        this.data[key].push(value);
    }

    cleanupEmptyData() {
        Object.keys(this.data).forEach((key) => {
            if (
                Array.isArray(this.data[key]) &&
                this.data[key].every((num) => num === 0)
            ) {
                delete this.data[key]; // Remove the key if the array contains only 0s
            }
        });
    }

    pushHistogramData(key: string, dataArray: number[]) {
        if (this.data[key] == undefined) {
            this.data[key] = [];
        }
        this.data[key].push(dataArray);
    }

    loadData(response: HttpResponse<any>) {
        const allData = response.body.data;
        console.log('all data', allData);

        const months = Object.keys(allData).sort();

        months.forEach((month) => {
            console.log('month', month);
            const data = allData[month];

            this.monthLabels.push(this.formatMonth(month));
            const physicalKeys = Object.keys(data.physical);
            const questionnaireKeys = Object.keys(data.questionnaire_slider);

            // PHYSICAL KEYS CALCULATIONS (heart rate etc)

            physicalKeys.forEach((physicalKey) => {
                const physicalData = data.physical[physicalKey];
                if (physicalData != 0) {
                    if (this.data[physicalKey] == undefined) {
                        this.data[physicalKey] = [];
                    }
                    this.data[physicalKey].push(Math.round(physicalData));

                    this.addMonthPerKey(physicalKey, month);
                }
            });

            // QUESTIONNAIRE CALCULATIONS

            questionnaireKeys.forEach((questionnaireKey) => {
                const questionnaireData =
                    data.questionnaire_slider[questionnaireKey];

                if (questionnaireData != 0) {
                    if (this.data[questionnaireKey] == undefined) {
                        this.data[questionnaireKey] = [];
                    }
                    this.data[questionnaireKey].push(questionnaireData);

                    this.addMonthPerKey(questionnaireKey, month);
                }
            });

            let questionnaireKey = 'questionnaire';

            if (data.questionnaire_total != 0) {
                if (this.data[questionnaireKey] == undefined) {
                    this.data[questionnaireKey] = [];
                }
                this.data[questionnaireKey].push(data.questionnaire_total);

                this.addMonthPerKey(questionnaireKey, month);
            }

            //HISTOGRAM CALCULATIONS

            this.processHistogramData(
                'social',
                Object.keys(this.socialMap),
                data.histogram.social,
                month
            );

            this.processHistogramData(
                'wherearebout',
                Object.keys(this.whereaboutsMap),
                data.histogram.whereabouts,
                month
            );

            this.processHistogramData(
                'sleep',
                Object.keys(this.sleepMap),
                data.histogram.sleep,
                month
            );

            // add all availbale months per histogram category - this is used in a case where we have one histogram, instead of it being split into several ones
            // this depends on how many "categories" are actually filled in
            this.addMonthPerKey('social', month);
            this.addMonthPerKey('wherearebout', month);
            this.addMonthPerKey('sleep', month);
        });

        this.calculateTotalsAndAverage();

        // clean any data that has just 0s
        this.cleanupEmptyData();

        // if there is less than 4 categories, we will display histogram as just one graph instead of it being split
        this.createHistogramsIfNecessary('social', this.socialKeys);
        this.createHistogramsIfNecessary('sleep', this.sleepMapKeys);
        this.createHistogramsIfNecessary(
            'wherearebout',
            this.whereaboutsMapKeys
        );

        console.log('this data', this.data);
        console.log('month labels', this.monthLabelsPerGraph);
    }

    processHistogramData(
        domainkey: string,
        mapKeys: string[],
        data: any,
        month: string
    ) {
        mapKeys.forEach((key, index) => {
            let id = index + 1;
            let identifier = `${domainkey}_` + id;
            let dataValue = data[key];

            this.pushToData(identifier, dataValue ?? 0);
            this.addMonthPerKey(identifier, month);
        });
    }

    createHistogramsIfNecessary(dataKey: string, allKeys: string[]) {
        let numberOfGraphsWithData = 0;

        let numberOfMonths = this.monthLabelsPerGraph[dataKey].length;
        allKeys.forEach((key, index) => {
            let exists = this.data[`${dataKey}_` + (index + 1)];
            if (exists) {
                numberOfGraphsWithData++;
            }
        });

        this.data[dataKey] = [];
        if (numberOfGraphsWithData <= 4) {
            allKeys.forEach((key, index) => {
                let exists = this.data[`${dataKey}_` + (index + 1)];

                if (exists) {
                    this.data[`${dataKey}`].push([...exists]);
                } else {
                    // push empty to account for empty categories
                    this.data[`${dataKey}`].push(
                        new Array(numberOfMonths).fill(0)
                    );
                }

                delete this.data[`${dataKey}_` + (index + 1)];
            });
        }
    }

    createGraphs() {
        let weekLabels = [] as string[];
        for (let i = 1; i <= 52; i++) {
            weekLabels.push('Week ' + i);
        }

        for (const [key, value] of Object.entries(this.data)) {
            let values = value as number[];
            let chartType = this.chart_type[key];

            if (chartType.type == 'line') {
                let labeles =
                    chartType.timeframe == 'week'
                        ? weekLabels
                        : this.monthLabelsPerGraph[key];
                this.charts[key] = this.createLineChart(
                    key,
                    this.monthLabelsPerGraph[key],
                    values,
                    chartType.showScaleY,
                    chartType.showDataTables,
                    chartType.color
                );
            } else if (chartType.type == 'histogram') {
                let labels = this.monthLabelsPerGraph[key + '_1'];
                console.log('labels', labels);
                let binLabels: any = null;

                if (key == 'social') {
                    binLabels = this.socialLabels;
                } else if (key == 'sleep') {
                    binLabels = this.sleepLabels;
                } else {
                    binLabels = this.whereaboutsLabels;
                }

                this.charts[key] = this.createMonthHistogram(
                    key,
                    labels,
                    binLabels
                );
            } else {
                this.charts[key] = this.createBarChart(
                    key,
                    this.monthLabelsPerGraph[key],
                    values,
                    chartType.color
                );
            }
        }
    }
    loadSubject(id) {
        this.subjectService.find(id).subscribe((subject: Subject) => {
            this.subject = subject;

            if (subject)
                this.subjectService
                    .findDataSummary(subject!.login!)
                    .subscribe((response: HttpResponse<any>) => {
                        this.loadData(response);
                        this.createGraphs();
                    });
        });
    }
    async ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.loadSubject(params['login']);
        });

        console.log('whereaboutkeys', this.whereaboutsMapKeys);
        console.log('sleepkeys', this.sleepMapKeys);
    }
}
