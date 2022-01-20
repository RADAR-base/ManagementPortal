import { MinimalProject } from '../project';

export interface MinimalOrganization {
    id?: number;
    name?: string;
}

export interface Organization extends MinimalOrganization {
    description?: string;
    location?: string;
    projects?: MinimalProject[];
}

export function copyOrganization(organization: Organization): Organization {
    return {
        ...organization,
        projects: organization.projects ? organization.projects.map(p => ({...p})) : organization.projects,
    }
}
