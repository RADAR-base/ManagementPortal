
const enum DataType {
    'RAW',
    'DERIVED'

};
export class SensorData {
    constructor(
        public id?: number,
        public sensorType?: string,
        public dataType?: DataType,
        public dataFormat?: string,
        public frequency?: string,
        public deviceTypeId?: number,
    ) {
    }
}
