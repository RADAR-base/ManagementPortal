import { copySourceType, SourceType } from '../../entities/source-type';
import { copyGroup, Group } from '../group';
import { copyOrganization, Organization } from '../organization';

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

export function copyProject(project: Project): Project {
    return {
        ...project,
        organization: project.organization ? copyOrganization(project.organization) : project.organization,
        groups: project.groups ? project.groups.map(copyGroup) : project.groups,
        sourceTypes: project.sourceTypes ? project.sourceTypes.map(copySourceType) : project.sourceTypes,
    }
}

export interface MinimalProject {
    id?: number;
    projectName?: string;
}
