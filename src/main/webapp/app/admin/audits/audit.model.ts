export class Audit {
    constructor(
        public data: Map<string, string>,
        public principal: string,
        public timestamp: string,
        public type: string,
    ) {
    }
}
