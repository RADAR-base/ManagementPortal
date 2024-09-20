import {copySourceType, SourceType} from '../../entities/source-type';
import {copyGroup, Group} from '../group';
import {MinimalOrganization} from '../organization';

export const enum ProjectStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'
}

export interface MinimalProject {
    id?: number;
    projectName?: string;
}

export interface Project extends MinimalProject {
    description?: string;
    organization?: MinimalOrganization;
    organizationName?: string;
    location?: string;
    startDate?: any;
    projectStatus?: ProjectStatus;
    endDate?: any;
    attributes?: any;
    sourceTypes?: SourceType[];
    groups?: Group[];
    humanReadableProjectName?: string;
    persistentTokenTimeout?: number;
}

export function copyProject(project: Project): Project {
    return {
        ...project,
        organization: project.organization ? {...project.organization} : project.organization,
        groups: project.groups ? project.groups.map(copyGroup) : project.groups,
        sourceTypes: project.sourceTypes ? project.sourceTypes.map(copySourceType) : project.sourceTypes,
    }
}

export interface MinimalProject {
    id?: number;
    projectName?: string;
}
