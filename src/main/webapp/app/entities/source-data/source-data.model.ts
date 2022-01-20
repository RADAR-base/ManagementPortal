import { MinimalSourceType } from '../source-type/source-type.model';

const enum ProcessingState {
    'RAW',
    'DERIVED',
    'VENDOR',
    'RADAR',
    'UNKNOWN'
}

export class SourceData {
    constructor(
        public id?: number,
        public sourceDataType?: string,
        public sourceDataName?: string,
        public processingState?: ProcessingState,
        public keySchema?: string,
        public valueSchema?: string,
        public topic?: string,
        public frequency?: string,
        public unit?: string,
        public sourceType?: MinimalSourceType,
    ) {
    }
}

export function copySourceData(sourceData: SourceData): SourceData {
    return {
        ...sourceData,
        sourceType: sourceData.sourceType ? {...sourceData.sourceType} : sourceData.sourceType,
    };
}
