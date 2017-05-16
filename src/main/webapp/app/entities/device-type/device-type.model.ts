
const enum SourceType {
    'ACTIVE',
    'PASSIVE'

};
import { SensorData } from '../sensor-data';
export class DeviceType {
    constructor(
        public id?: number,
        public deviceProducer?: string,
        public deviceModel?: string,
        public sourceType?: SourceType,
        public sensorData?: SensorData,
    ) {
    }
}
