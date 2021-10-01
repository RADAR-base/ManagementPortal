import { Role } from '../../admin/user-management/role.model';
import { Project } from '../project/project.model';
import { Dictionary } from '../dictionary-mapper/dictionary-mapper.model';
import { MinimalSource } from '../source/source.model';

export class Subject {
    public id?: any;
    public login?: string;
    public externalLink?: string;
    public externalId?: string;
    public createdBy?: string;
    public createdDate?: Date;
    public dateOfBirth?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public group?: string;
    public password?: string;
    public project?: Project;
    public sources?: MinimalSource[];
    public attributes: Dictionary;
    public status: SubjectStatus;
    public roles?: Role[];

    constructor(
            id?: number,
            login?: string,
            externalLink?: string,
            externalId?: string,
            status?: SubjectStatus,
            createdBy?: string,
            createdDate?: Date,
            dateOfBirth?: Date,
            lastModifiedBy?: string,
            lastModifiedDate?: Date,
            group?: string,
            password?: string,
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
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.group = group ? group : null;
        this.password = password ? password : null;
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

export class Group {
    public id?: any;
    public groupName?: string;
}
