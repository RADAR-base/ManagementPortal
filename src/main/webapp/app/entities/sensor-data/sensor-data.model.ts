
const enum DataType {
    'RAW',
    'DERIVED'

};
import { DeviceType } from '../device-type';
export class SensorData {
    constructor(
        public id?: number,
        public sensorType?: string,
        public dataType?: DataType,
        public dataFormat?: string,
        public frequency?: string,
        public deviceType?: DeviceType,
    ) {
    }
}
