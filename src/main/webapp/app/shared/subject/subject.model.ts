import { Role } from '../../admin/user-management/role.model';
import { Project } from '../project/project.model';
import { MinimalSource } from '../source/source.model';

export class Subject {
    id?: any;
    login?: string;
    externalLink?: string;
    externalId?: string;
    createdBy?: string;
    createdDate?: Date;
    dateOfBirth?: Date;
    enrollmentDate?: Date;
    lastModifiedBy?: string;
    lastModifiedDate?: Date;
    group?: string;
    password?: string;
    personName?: string;
    project?: Project;
    sources: MinimalSource[];
    attributes?: Record<string, string>;
    status: SubjectStatus;
    roles?: Role[];

    constructor(
            id?: number,
            login?: string,
            externalLink?: string,
            externalId?: string,
            status?: SubjectStatus,
            createdBy?: string,
            createdDate?: Date,
            dateOfBirth?: Date,
            enrollmentDate?: Date,
            lastModifiedBy?: string,
            lastModifiedDate?: Date,
            group?: string,
            password?: string,
            personName?: string,
            project?: Project,
            sources?: MinimalSource[],
            roles?: Role[],
    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.externalLink = externalLink ? externalLink : null;
        this.externalId = externalId ? externalId : null;
        this.createdBy = createdBy ? createdBy : null;
        this.createdDate = createdDate ? createdDate : null;
        this.dateOfBirth = dateOfBirth ? dateOfBirth : null;
        this.enrollmentDate = enrollmentDate ? enrollmentDate : null;
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.group = group ? group : null;
        this.password = password ? password : null;
        this.personName = personName ? personName : null;
        this.project = project ? project : null;
        this.sources = sources ? sources : [];
        this.status = status ? status : SubjectStatus.DEACTIVATED;
        this.roles = roles;
    }
}

export const enum SubjectStatus {
    'DEACTIVATED',
    'ACTIVATED',
    'DISCONTINUED',
    'INVALID'
}
