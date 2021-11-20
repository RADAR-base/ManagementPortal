
export class Organization {
    constructor(
        public id?: number,
        public name?: string,
        public description?: string,
        public location?: string,
    ) {
    }
}

export const ORGANIZATIONS: Organization[] = [
    {
        id: 1,
        name: 'The Hyve',
        description: 'The Hyve description',
        location: 'Utrecht, The Netherlands'
    },
    {
        id: 2,
        name: 'Another Organization',
        description: 'Another organization\'s description',
        location: 'A City'
    }
]
