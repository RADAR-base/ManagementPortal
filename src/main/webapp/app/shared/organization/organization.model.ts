import { copyProject, Project } from '../project';

export interface Organization {
    id?: number;
    name?: string;
    description?: string;
    location?: string;
    projects?: Project[];
}

export function copyOrganization(organization: Organization): Organization {
    return {
        ...organization,
        projects: organization.projects ? organization.projects.map(copyProject) : organization.projects,
    }
}
