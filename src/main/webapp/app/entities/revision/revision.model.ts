export class Revision {
    constructor(
        public id: number,
        public timestamp: Date,
        public author: string,
        public changes: any
    ) {}
}
