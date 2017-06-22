export class Source {
    constructor(
        public id?: number,
        public sourceId?: string,
        public deviceCategory?: string,
        public assigned?: boolean,
        public deviceTypeId?: number,
        public projectId?: number,
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
