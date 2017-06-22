
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
        public projectAdmin?: number,
        public deviceTypeId?: number,
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
