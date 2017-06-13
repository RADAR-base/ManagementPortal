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

export class MinimalDevice {
    constructor(
        public id?: number,
        public deviceTypeAndPhysicalId?: string,
        public assigned?: boolean,
    ) {
        this.assigned = false;
    }
}
