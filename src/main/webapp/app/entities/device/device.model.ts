import { DeviceType } from '../device-type';
import { Study } from '../study';
export class Device {
    constructor(
        public id?: number,
        public devicePhysicalId?: string,
        public deviceCategory?: string,
        public deviceType?: DeviceType,
        public study?: Study,
    ) {
    }
}
