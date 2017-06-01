export class Device {
    constructor(
        public id?: number,
        public devicePhysicalId?: string,
        public deviceCategory?: string,
        public activated?: boolean,
        public deviceTypeId?: number,
        public projectId?: number,
    ) {
        this.activated = false;
    }
}
