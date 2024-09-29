import {copySourceData, SourceData} from '../source-data';

const enum SourceTypeScope {
    'ACTIVE',
    'PASSIVE'
}

export interface SourceType {
    id?: number,
    producer?: string,
    model?: string,
    catalogVersion?: string,
    sourceTypeScope?: SourceTypeScope,
    sourceData?: SourceData[],
    projectId?: number,
    canRegisterDynamically?: boolean,
    name?: string,
    description?: string,
    assessmentType?: string,
    appProvider?: string
}

export function copySourceType(sourceType: SourceType) {
    return {
        ...sourceType,
        sourceData: sourceType.sourceData ? sourceType.sourceData.map(copySourceData) : sourceType.sourceData,
    }
}

export interface MinimalSourceType {
    id?: number,
    producer?: string,
    model?: string,
    catalogVersion?: string,
}
