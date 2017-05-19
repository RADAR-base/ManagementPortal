import { DeviceType } from '../device-type';
import {Project} from "../project/project.model";
export class Device {
    constructor(
        public id?: number,
        public devicePhysicalId?: string,
        public deviceCategory?: string,
        public activated?: boolean,
        public deviceType?: DeviceType,
        public project?: Project,
    ) {
        this.activated = false;
    }
}
