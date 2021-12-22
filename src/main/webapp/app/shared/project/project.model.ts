import {SourceType} from '../../entities/source-type';
import { Group } from '../group';
import { Organization } from "../organization";

export const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'
}

export interface Project {
    id?: number;
    projectName?: string;
    description?: string;
    organization?: Organization;
    organizationName?: string;
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
