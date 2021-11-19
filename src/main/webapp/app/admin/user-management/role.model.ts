export class Role {
    constructor(
            public id?: number,
            public authorityName?: string,
            public projectId?: number,
            public projectName?: string,
            public organizationId?: number,
            public organizationName?: string,
    ) {
    }
}
