
import {Attribute} from "../../shared/attribute-mapper/attribute-mapper.model";
const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'

};
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
        public deviceTypeId?: number,
        public attributes ?: Attribute[]
    ) {
    }
}

export class MinimalProject {
    constructor(
        public id?: number,
        public projectName?: string,
    ) {
    }
}
