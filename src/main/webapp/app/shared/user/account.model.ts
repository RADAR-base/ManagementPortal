import { Role } from "../../admin/user-management/role.model";

export interface Account {
    id?: number;
    login: string,
    firstName?: string,
    lastName?: string,
    email?: string,
    activated?: boolean,
    langKey?: string;
    createdBy?: string;
    createdDate?: string;
    lastModifiedBy?: string;
    lastModifiedDate?: string;
    roles?: Role[];
    authorities?: string[];
}
