
const enum DataType {
    'RAW',
    'DERIVED'

};
export class SensorData {
    constructor(
        public id?: number,
        public sensorName?: string,
        public sensorType?: string,
        public dataType?: DataType,
        public keySchema?: string,
        public frequency?: string,
        public deviceTypeId?: number,
    ) {
    }
}
