import { MinimalProject } from '../project/project.model';
import { SourceType } from '../../entities/source-type/source-type.model';

export class Source {
    constructor(
            public id?: number,
            public sourceId?: string,
            public sourceName?: string,
            public expectedSourceName?: string,
            public assigned?: boolean,
            public sourceType?: SourceType,
            public project?: MinimalProject,
            public attributes?: Record<string, string>,
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
            public sourceTypeCatalogVersion?: string,
            public expectedSourceName?: string | null,
            public sourceId?: string,
            public sourceName?: string,
            public assigned?: boolean,
    ) {
        this.assigned = false;
    }
}
