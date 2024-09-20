import {Role} from '../../admin/user-management/role.model';
import {Project} from '../project';

export interface User {
    id?: number;
    login?: string;
    identity?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    activated?: Boolean;
    langKey?: string;
    authorities: string[];
    roles?: Role[];
    createdBy?: string;
    createdDate?: Date;
    lastModifiedBy?: string;
    lastModifiedDate?: Date;
    password?: string;
    project?: Project;
}
