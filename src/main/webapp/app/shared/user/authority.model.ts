export enum Scope {
    GLOBAL = 'GLOBAL',
    ORGANIZATION = 'ORGANIZATION',
    PROJECT = 'PROJECT',
}

export interface Authority {
    authority: string;
    scope?: Scope;
}
