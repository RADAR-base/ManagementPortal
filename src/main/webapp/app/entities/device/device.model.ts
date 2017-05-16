import { DeviceType } from '../device-type';
export class Device {
    constructor(
        public id?: number,
        public devicePhysicalId?: string,
        public deviceCategory?: string,
        public deviceType?: DeviceType,
    ) {
    }
}
