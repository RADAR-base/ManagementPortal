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

const whereaboutsMap = {
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

const socialMap = {
    '1': 'On my own',
    '2': 'With strangers',
    '3': 'With people I know',
    '4': 'With people I am close to',
    '5': 'Connecting with people online or using social media',
};

const sleepMap = {
    '0-2': '0-2 hrs',
    '2-4': '2-4 hrs',
    '4-6': '4-6 hrs',
    '6-8': '6-8 hrs',
    '8-10': '8-10 hrs',
    '10-12': '10-12 hrs',
    '12-14': '12-14 hrs',
    '14+': '14+ hrs+',
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
        social_1: barGraph,
        social_2: barGraph,
        social_3: barGraph,
        social_4: barGraph,
        social_5: barGraph,
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
    socialKeys = Object.keys(this.socialMap);
    subject: Subject;
    private subscription: any;

    constructor(
        private subjectService: SubjectService,
        private route: ActivatedRoute
    ) {}

    createSocialHistogram() {
        let months = [
            'Jan',
            'Feb',
            'Mar',
            'Apr',
            'May',
            'Jun',
            'Jul',
            'Aug',
            'Sep',
            'Oct',
            'Nov',
            'Dec',
        ];

        let binLabels2 = [
            'On my own',
            'with strangers',
            'with people I know',
            'with people I am close to',
            'Connecting with people online or using social media',
        ];

        // Define colors for each bin
        const binColors = [
            'rgba(255, 99, 132, 0.6)', // 0-2
            'rgba(54, 162, 235, 0.6)', // 2-4
            'rgba(255, 206, 86, 0.6)', // 4-6
            'rgba(75, 192, 192, 0.6)', // 6-8
            'rgba(153, 102, 255, 0.6)', // 8-10
        ];

        // Generate random data for each month
        const data = binLabels2.map(() =>
            Array.from({ length: 12 }, () => Math.floor(Math.random() * 10 + 1))
        );

        return {
            type: 'bar',
            data: {
                labels: months,
                datasets: binLabels2.map((label, index) => ({
                    label: label,
                    data: data[index],
                    backgroundColor: binColors[index],
                    borderColor: binColors[index].replace('0.6', '1'),
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

    createSleepHistogram() {
        let months = [
            'Jan',
            'Feb',
            'Mar',
            'Apr',
            'May',
            'Jun',
            'Jul',
            'Aug',
            'Sep',
            'Oct',
            'Nov',
            'Dec',
        ];

        let binLabels2 = [
            '0-2',
            '2-4',
            '4-6',
            '6-8',
            '8-10',
            '10-12',
            '12-14',
            '14+',
        ];

        // Define colors for each bin
        const binColors = [
            'rgba(255, 99, 132, 0.6)', // 0-2
            'rgba(54, 162, 235, 0.6)', // 2-4
            'rgba(255, 206, 86, 0.6)', // 4-6
            'rgba(75, 192, 192, 0.6)', // 6-8
            'rgba(153, 102, 255, 0.6)', // 8-10
            'rgba(255, 159, 64, 0.6)', // 10-12
            'rgba(199, 199, 199, 0.6)', // 12-14
            'rgba(83, 255, 157, 0.6)', // 14+
        ];

        // Generate random data for each month
        const data = binLabels2.map(() =>
            Array.from({ length: 12 }, () => Math.floor(Math.random() * 10 + 1))
        );

        return {
            type: 'bar',
            data: {
                labels: months,
                datasets: binLabels2.map((label, index) => ({
                    label: label,
                    data: data[index],
                    backgroundColor: binColors[index],
                    borderColor: binColors[index].replace('0.6', '1'),
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

    createMonthHistogram() {
        // Sample data for each month
        let months = [
            'Jan',
            'Feb',
            'Mar',
            'Apr',
            'May',
            'Jun',
            'Jul',
            'Aug',
            'Sep',
            'Oct',
            'Nov',
            'Dec',
        ];

        let binLabels2 = ['Relaxing'];

        // Define colors for each bin
        const binColors = [
            'rgba(255, 99, 132, 0.6)', // 0-2
            'rgba(54, 162, 235, 0.6)', // 2-4
            'rgba(255, 206, 86, 0.6)', // 4-6
            'rgba(75, 192, 192, 0.6)', // 6-8
            'rgba(153, 102, 255, 0.6)', // 8-10
            'rgba(255, 159, 64, 0.6)', // 10-12
            'rgba(199, 199, 199, 0.6)', // 12-14
            'rgba(83, 255, 157, 0.6)', // 14+m
            'rgba(203, 255, 83, 0.6)', // 14+
            'rgba(255, 83, 183, 0.6)', // 14+
            'rgba(83, 203, 255, 0.6)', // 14+
            'rgba(83, 203, 255, 0.6)', // 14+
        ];

        // Generate random data for each month
        const data = binLabels2.map(() =>
            Array.from({ length: 12 }, () => Math.floor(Math.random() * 10 + 1))
        );

        return {
            type: 'bar',
            data: {
                labels: months,
                datasets: binLabels2.map((label, index) => ({
                    label: label,
                    data: data[index],
                    backgroundColor: binColors[index],
                    borderColor: binColors[index].replace('0.6', '1'),
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

    createHistogram() {
        // Sample data
        const rawData = [
            12, 19, 3, 5, 2, 3, 10, 15, 18, 14, 7, 9, 12, 17, 22, 25, 18, 10, 8,
            6,
        ];

        // Define bins
        const binSize = 5;
        const minValue = Math.min(...rawData);
        const maxValue = Math.max(...rawData);
        //const bins = ["On my own", "With strangers", "With people I know", "with people I am close to ", "Connecting with people online or using social media"];

        const bins = [];
        console.log('bins', bins);

        for (let i = minValue; i <= maxValue; i += binSize) {
            //@ts-ignore
            bins.push({ range: `${i}-${i + binSize - 1}`, count: 0 });
        }

        // Populate bins
        rawData.forEach((value) => {
            const index = Math.floor((value - minValue) / binSize);
            //@ts-ignore
            bins[index].count++;
        });

        console.log('raw data', rawData);

        //@ts-ignore
        const labels = [
            'On my own',
            'With strangers',
            'With people I know',
            'with people I am close to ',
            'Connecting with people online or using social media',
        ];

        const data = [5, 9, 15, 2, 5];

        //@ts-ignore
        const ctx = document.getElementById('histogramChart').getContext('2d');

        this.charts['histogramChart'] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Frequency',
                        data: data,
                        backgroundColor: 'rgba(75, 192, 192, 0.6)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1,
                    },
                ],
            },
            options: {
                devicePixelRatio: 4,
                plugins: {
                    legend: {
                        display: false,
                        position: 'bottom',
                    },
                },
                responsive: false,
                maintainAspectRatio: true,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                },
            },
        });
    }

    createLineChart(
        id: string,
        labels: string[],
        data: number[],
        showScaleY: boolean,
        showDataLables: boolean,
        color: string = 'rgba(110, 37, 147, 0.9)'
    ) {
        //@ts-ignore
        // const ctx = document
        //     .getElementById('linear_gradient')
        //     ?.getContext('2d');

        // const gradient = ctx.createLinearGradient(0, 0, 0, 400);
        // gradient.addColorStop(0, color);
        // gradient.addColorStop(1, 'rgba(255, 255, 255, 0.0)');

        let graphObject = {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        fill: true, // Enables the fill under the line
                        tension: 0.2,
                        data: data,
                        borderWidth: 2,
                        borderColor: color, // Line color
                        //     backgroundColor: gradient,
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
                        // Alignment relative to the bar (top, middle, bottom)
                        formatter: (value) => value, // Format the value (e.g., add units or rounding)
                        color: '#000', // Text color
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
                // Alignment relative to the bar (top, middle, bottom)
                formatter: (value) => value, // Format the value (e.g., add units or rounding)
                color: '#000', // Text color
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
                        barThickness: 30, // Fixed bar width,
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
                        anchor: 'end', // Positioning of the label (start, center, end)
                        // Alignment relative to the bar (top, middle, bottom)
                        formatter: (value) => value, // Format the value (e.g., add units or rounding)
                        color: '#000', // Text color
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

    loadData(response: HttpResponse<any>) {
        const allData = response.body.data;

        const months = Object.keys(allData).sort(); // Sorts the months in order

        // Step 2: Iterate over each month in chronological order
        months.forEach((month) => {
            const data = allData[month];

            this.monthLabels.push(this.formatMonth(month));
            const physicalKeys = Object.keys(data.physical);
            const questionnaireKeys = Object.keys(data.questionnaire_slider);

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

            console.log('socialkeys', Object.keys(data.histogram.social));
            const socialKeys = Object.keys(socialMap);

            socialKeys.forEach((key) => {
                let identifier = 'social_' + key;
                let socialData = data.histogram.social[key];

                this.pushToData(identifier, socialData ?? 0);
                this.addMonthPerKey(identifier, month);

                //  this.addMonthPerKey(questionnaireKey, month);
            });
        });

        this.calculateTotalsAndAverage();

        this.cleanupEmptyData();
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
    }
}
