import {Project} from "../../entities/project/project.model";
import {Role} from "../../admin/user-management/role.model";
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
        password?: string,
        project?: Project
    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.firstName = firstName ? firstName : null;
        this.lastName = lastName ? lastName : null;
        this.email = email ? email : null;
        this.activated = activated ? activated : false;
        this.langKey = langKey ? langKey : null;
        this.roles = roles ? roles : null;
        this.authorities = authorities? authorities : null;
        this.password = password ? password : null;
        this.project = project ? project: new Project();
    }
}
