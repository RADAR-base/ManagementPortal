
const enum SourceTypeClass {
    'ACTIVE',
    'PASSIVE'
};

export class DeviceType {
    constructor(
        public id?: number,
        public deviceProducer?: string,
        public deviceModel?: string,
        public catalogVersion?: string,
        public sourceTypeClass?: SourceTypeClass,
        public sourceDataId?: number,
        public projectId?: number,
        public canRegisterDynamically?: boolean
    ) {
    }
}
