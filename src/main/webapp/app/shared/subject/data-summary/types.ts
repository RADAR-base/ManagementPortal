export interface Graph {
    type: 'bar' | 'line' | 'histogram';
    showScaleY: boolean;
    showDataTables: boolean;
    color?: string;
    timeframe?: 'week' | 'month';
}
