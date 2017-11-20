
const enum ProcessingState {
    'RAW',
    'DERIVED'

};
export class SourceData {
    constructor(
        public id?: number,
        public sourceDataType?: string,
        public sensorType?: string,
        public sourceDataName?: string,
        public processingState?: ProcessingState,
        public keySchema?: string,
        public frequency?: string,
        public sourceTypeId?: number,
    ) {
    }
}
