
const enum SourceType {
    'ACTIVE',
    'PASSIVE'
};

export class DeviceType {
    constructor(
        public id?: number,
        public deviceProducer?: string,
        public deviceModel?: string,
        public catalogVersion?: string,
        public sourceType?: SourceType,
        public sourceDataId?: number,
        public projectId?: number,
        public canRegisterDynamically?: boolean
    ) {
    }
}
