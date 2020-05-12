import {SourceType} from '../../entities/source-type';

export const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'
}

export class Project {
    constructor(
            public id?: number,
            public projectName?: string,
            public description?: string,
            public organization?: string,
            public location?: string,
            public startDate?: any,
            public projectStatus?: ProjectStatus,
            public endDate?: any,
            public attributes ?: any,
            public sourceTypes?: SourceType[],
            public humanReadableProjectName ?: string,
    ) {
    }
}

export class MinimalProject {
    constructor(
            public id?: number,
            public projectName?: string,
    ) {
    }
}
