import {SourceType} from '../../entities/source-type';
import { Group } from '../group';

export const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'
}

export interface Project {
    id?: number;
    projectName?: string;
    description?: string;
    organization?: string;
    location?: string;
    startDate?: any;
    projectStatus?: ProjectStatus;
    endDate?: any;
    attributes ?: any;
    sourceTypes?: SourceType[];
    groups?: Group[];
    humanReadableProjectName ?: string;
    persistentTokenTimeout?: number;
}

export interface MinimalProject {
    id?: number;
    projectName?: string;
}
