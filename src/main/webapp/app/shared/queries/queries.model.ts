import { Subject } from '../subject';
import { User } from '../user/user.model';

export interface Query {}

export interface QueryGroup {
    id?: any;
    name?: string;
    description?: string;
    createdDate?: Date;
    updatedDate?: Date;
    createdBy?: Subject;
    updatedBy?: Subject;
}

export interface QueryParticipant {
    id?: any;
    queryGroupId?: number;
    subjectId?: number;
    createdBy?: User;
}

export interface QueryString {
    field?: string;
    operator?: string;
    timeFame?: any;
    value?: any;
    rules?: [QueryString];
}
