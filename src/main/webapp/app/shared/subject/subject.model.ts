import {Project} from "../../entities/project/project.model";
import {MinimalSource} from "../source/source.model";
import {Dictionary} from "../dictionary-mapper/dictionary-mapper.model";
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
    public project?: Project;
    public sources?: MinimalSource[];
    public attributes: Dictionary;
    public status: SubjectStatus;

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
         project?: Project,
         sources?: MinimalSource[]

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
        this.project = project ? project: null;
        this.sources = sources ? sources: [];
        this.status = status ? status: SubjectStatus.DEACTIVATED;
    }
}
export const enum SubjectStatus {
    'DEACTIVATED',
    'ACTIVATED',
    'DISCONTINUED',
    'INVALID'
}
