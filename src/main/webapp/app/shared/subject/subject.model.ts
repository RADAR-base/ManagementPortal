import {Project} from "../../entities/project/project.model";
import {MinimalSource} from "../source/source.model";
import {Dictionary} from "../dictionary-mapper/dictionary-mapper.model";
import {Role} from "../../admin/user-management/role.model";
export class Subject {
    public id?: any;
    public login?: string;
    public externalLink?: string;
    public externalId?: string;
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
         password?: string,
         project?: Project,
         sources?: MinimalSource[],
         roles?: Role[]

    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.externalLink = externalLink ? externalLink : null;
        this.externalId = externalId ? externalId : null;
        this.password = password ? password : null;
        this.project = project ? project: null;
        this.sources = sources ? sources: [];
        this.status = status ? status: SubjectStatus.DEACTIVATED;
        this.roles = roles;
    }
}
export const enum SubjectStatus {
    'DEACTIVATED',
    'ACTIVATED',
    'DISCONTINUED',
    'INVALID'
}
