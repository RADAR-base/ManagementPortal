import {SourceType} from "../../entities/source-type/source-type.model";
import {MinimalProject} from "../../entities/project/project.model";
export class Source {
    constructor(
        public id?: number,
        public sourceId?: string,
        public sourceName?: string,
        public expectedSourceName?: string,
        public assigned?: boolean,
        public sourceType?: SourceType,
        public project?: MinimalProject,
    ) {
        this.assigned = false;
    }
}

export class MinimalSource {
    constructor(
        public id?: number,
        public sourceType?: number,
        public sourceTypeProducer?: string,
        public sourceTypeModel?: string,
        public sourceTypeVersion?: string,
        public expectedSourceName?: string | null,
        public sourceId?: string,
        public sourceName?: string,
        public assigned?: boolean,
    ) {
        this.assigned = false;
    }
}
