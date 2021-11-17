
export class Organization {
    constructor(
        public id?: number,
        public organizationName?: string,
        public description?: string,
        public location?: string,
    ) {
    }
}

export const ORGANIZATIONS: Organization[] = [
    {
        id: 1,
        organizationName: 'The Hyve',
        description: 'The Hyve description',
        location: 'Utrecht, The Netherlands'
    },
    {
        id: 2,
        organizationName: 'Another Organization',
        description: 'Another organization\'s description',
        location: 'A City'
    }
]
