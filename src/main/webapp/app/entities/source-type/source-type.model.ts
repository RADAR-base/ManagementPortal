
const enum SourceTypeScope {
    'ACTIVE',
    'PASSIVE'
}

export class SourceType {
    constructor(
        public id?: number,
        public producer?: string,
        public model?: string,
        public catalogVersion?: string,
        public sourceTypeScope?: SourceTypeScope,
        public sourceDataId?: number,
        public projectId?: number,
        public canRegisterDynamically?: boolean,
        public name?: string,
        public description?: string,
        public assessmentType?: string,
        public appProvider?: string
    ) {
    }
}

export class MinimalSourceType {
    constructor(
        public id?: number,
        public producer?: string,
        public model?: string,
        public catalogVersion?: string,
    ) {
    }
}
