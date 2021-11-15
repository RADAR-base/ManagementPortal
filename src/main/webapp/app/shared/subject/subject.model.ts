import { Role } from '../../admin/user-management/role.model';
import { Project } from '../project/project.model';
import { MinimalSource } from '../source/source.model';
import { NgbDateStruct } from "@ng-bootstrap/ng-bootstrap";

export interface Subject {
    id?: any;
    login?: string;
    externalLink?: string;
    externalId?: string;
    createdBy?: string;
    createdDate?: Date;
    dateOfBirth?: Date;
    enrollmentDate?: Date;
    lastModifiedBy?: string;
    lastModifiedDate?: Date;
    group?: string;
    password?: string;
    personName?: string;
    project?: Project;
    sources: MinimalSource[];
    attributes?: Record<string, string>;
    status: SubjectStatus;
    roles?: Role[];
}

export interface SubjectFilterCriteria {
    externalId: string
    dateOfBirth?: NgbDateStruct
    subjectId: string
    enrollmentDateFrom?: NgbDateStruct
    enrollmentDateTo?: NgbDateStruct
    groupId: string
    groupName: string
    personName: string
    humanReadableId: string
}

export const enum SubjectStatus {
    'DEACTIVATED',
    'ACTIVATED',
    'DISCONTINUED',
    'INVALID'
}
