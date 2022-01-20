export enum Scope {
    GLOBAL = 'GLOBAL',
    ORGANIZATION = 'ORGANIZATION',
    PROJECT = 'PROJECT',
}

export interface Authority {
    name: string;
    scope?: Scope;
}
