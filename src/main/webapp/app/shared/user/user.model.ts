import { Role } from '../../admin/user-management/role.model';
import { Project } from '../../shared/project';

export class User {
    public id?: any;
    public login?: string;
    public firstName?: string;
    public lastName?: string;
    public email?: string;
    public activated?: Boolean;
    public langKey?: string;
    public authorities: string[];
    public roles?: Role[];
    public createdBy?: string;
    public createdDate?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public password?: string;
    public project?: Project = new Project();

    constructor(
            id?: any,
            login?: string,
            firstName?: string,
            lastName?: string,
            email?: string,
            activated?: Boolean,
            langKey?: string,
            authorities?: string[],
            roles?: Role[],
            createdBy?: string,
            createdDate?: Date,
            lastModifiedBy?: string,
            lastModifiedDate?: Date,
            password?: string,
            project?: Project,
    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.firstName = firstName ? firstName : null;
        this.lastName = lastName ? lastName : null;
        this.email = email ? email : null;
        this.activated = activated ? activated : false;
        this.langKey = langKey ? langKey : null;
        this.roles = roles ? roles : null;
        this.authorities = authorities ? authorities : null;
        this.createdBy = createdBy ? createdBy : null;
        this.createdDate = createdDate ? createdDate : null;
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.password = password ? password : null;
        this.project = project ? project : new Project();
    }
}
