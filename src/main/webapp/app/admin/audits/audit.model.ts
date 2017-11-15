export class Audit {
    constructor(
        public data: any,
        public principal: string,
        public timestamp: string,
        public type: string
    ) { }
}
