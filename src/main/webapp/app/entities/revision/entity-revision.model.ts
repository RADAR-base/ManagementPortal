/**
 * Information about an entity at a specific revision
 */
export class EntityRevision {
    constructor(
        public id: number,
        public author: string,
        public timestamp: Date,
        public revisionType: string,
        public entity: any
    ) {}
}