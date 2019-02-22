import { Role } from '../../admin/user-management/role.model';
import { Dictionary } from '../dictionary-mapper/dictionary-mapper.model';
import { MinimalSource } from '../source/source.model';

export class Subject {
    public id?: any;
    public login?: string;
    public externalLink?: string;
    public externalId?: string;
    public createdBy?: string;
    public createdDate?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public password?: string;
    public projectName?: string;
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
            lastModifiedBy?: string,
            lastModifiedDate?: Date,
            password?: string,
            projectName?: string,
            sources?: MinimalSource[],
            roles?: Role[],
    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.externalLink = externalLink ? externalLink : null;
        this.externalId = externalId ? externalId : null;
        this.createdBy = createdBy ? createdBy : null;
        this.createdDate = createdDate ? createdDate : null;
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.password = password ? password : null;
        this.projectName = projectName ? projectName : null;
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
