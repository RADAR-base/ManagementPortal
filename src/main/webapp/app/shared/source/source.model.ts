import {DeviceType} from "../../entities/device-type/device-type.model";
import {MinimalProject} from "../../entities/project/project.model";
export class Source {
    constructor(
        public id?: number,
        public sourceId?: string,
        public sourceName?: string,
        public expectedSourceName?: string,
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
        public deviceType?: number,
        public deviceTypeName?: string,
        public expectedSourceName?: string | null,
        public sourceId?: string,
        public sourceName?: string,
        public assigned?: boolean,
    ) {
        this.assigned = false;
    }
}
