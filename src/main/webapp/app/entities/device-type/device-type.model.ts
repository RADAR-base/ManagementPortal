
const enum SourceTypeScope {
    'ACTIVE',
    'PASSIVE'
};

export class DeviceType {
    constructor(
        public id?: number,
        public deviceProducer?: string,
        public deviceModel?: string,
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
