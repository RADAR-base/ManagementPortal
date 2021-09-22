import {SourceType} from '../../entities/source-type';
import { Group } from '../subject';

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
            public groups?: Group[],
            public humanReadableProjectName ?: string,
            public persistentTokenTimeout?: number
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
