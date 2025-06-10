import { Subject } from '../subject';
import { User } from '../user/user.model';


type optionalString = string | null | undefined

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
    createdBy?: User
}

export type QueryNode =
  | {
      query: QueryDTO;
    }
  | {
      logic_operator: string;
      children: QueryNode[];
    };


export interface QueryDTO {
    field?: optionalString,
    operator?: optionalString,
    timeFrame?: optionalString,
    value?: optionalString,
    logic_operator?: optionalString,
    children?: QueryDTO[],
    entity?: optionalString

}

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
    rules?: QueryString[];
    condition?: string;
    entity?: string;
}


export enum ContentType {
    TITLE = "TITLE",
    PARAGRAPH = 'PARAGRAPH',
    IMAGE = 'IMAGE',
    VIDEO = 'VIDEO',
}


export interface ContentItem {
    id?: number;
    heading?: String;
    type: ContentType;
    value?: String | Number;
    imageValue?: String;
    imageBlob?: String;
    isValidImage?: Boolean;
    queryGroupId?: Number;
}


