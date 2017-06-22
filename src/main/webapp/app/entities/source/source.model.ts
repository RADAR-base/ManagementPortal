import {MinimalProject} from "../project/project.model";
import {DeviceType} from "../device-type/device-type.model";
export class Source {
    constructor(
        public id?: number,
        public sourceId?: string,
        public deviceCategory?: string,
        public assigned?: boolean,
        public deviceType?: DeviceType,
        public project?: MinimalProject,
    ) {
        this.assigned = false;
    }
}

export class MinimalSource {
    constructor(
        public id?: number,
        public deviceTypeAndSourceId?: string,
        public assigned?: boolean,
    ) {
        this.assigned = false;
    }
}
