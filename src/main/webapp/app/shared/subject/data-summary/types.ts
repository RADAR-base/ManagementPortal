export interface Graph {
    type: "bar" | "line",
    showScaleY: boolean,
    showDataTables: boolean,
    color?: string,
    timeframe?: "week" | "month",

}
