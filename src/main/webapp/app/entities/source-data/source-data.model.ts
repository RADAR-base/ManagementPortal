
import {MinimalSourceType, SourceType} from "../source-type/source-type.model";

const enum ProcessingState {
    'RAW',
    'DERIVED'

};
export class SourceData {
    constructor(
        public id?: number,
        public sourceDataType?: string,
        public sourceDataName?: string,
        public processingState?: ProcessingState,
        public keySchema?: string,
        public frequency?: string,
        public sourceType?: MinimalSourceType,
    ) {
    }
}
