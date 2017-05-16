
const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'

};
import { Device } from '../device';
export class Project {
    constructor(
        public id?: number,
        public projectName?: string,
        public description?: string,
        public organization?: string,
        public location?: string,
        public startDate?: any,
        public projectStatus?: ProjectStatus,
        public endDate?: any,
        public projectOwner?: number,
        public device?: Device,
    ) {
    }
}
