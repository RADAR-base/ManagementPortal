export class Device {
    constructor(
        public id?: number,
        public devicePhysicalId?: string,
        public deviceCategory?: string,
        public assigned?: boolean,
        public deviceTypeId?: number,
        public projectId?: number,
    ) {
        this.assigned = false;
    }
}
