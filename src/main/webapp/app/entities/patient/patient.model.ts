import {Project} from "../project/project.model";
import {Device, MinimalDevice} from "../device/device.model";
export class Patient {
    public id?: any;
    public login?: string;
    public email?: string;
    public externalLink?: string;
    public externalId?: string;
    public removed?: boolean;
    public activated?: Boolean;
    public createdBy?: string;
    public createdDate?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public password?: string;
    public project?: Project;
    public device?: MinimalDevice = new MinimalDevice();

    constructor(
         id?: number,
         login?: string,
         email?: string,
         externalLink?: string,
         externalId?: string,
         removed?: boolean,
         activated?: boolean,
         createdBy?: string,
         createdDate?: Date,
         lastModifiedBy?: string,
         lastModifiedDate?: Date,
         password?: string,
         project?: Project,
         device?: Device

    ) {
        this.id = id ? id : null;
        this.login = login ? login : null;
        this.email = email ? email : null;
        this.externalLink = externalLink ? externalLink : null;
        this.externalId = externalId ? externalId : null;
        this.removed = removed ? removed: false;
        this.activated = activated ? activated : false;
        this.createdBy = createdBy ? createdBy : null;
        this.createdDate = createdDate ? createdDate : null;
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.password = password ? password : null;
        this.project = project ? project: null;
        this.device = device ? device: new MinimalDevice();
    }
}
