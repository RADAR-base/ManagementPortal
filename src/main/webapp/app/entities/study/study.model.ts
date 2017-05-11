
const enum StudyStatus {
    'PLANNING',
    'ONGOING',
    'ENDED'

};
import { Project } from '../project';
import { Device } from '../device';
export class Study {
    constructor(
        public id?: number,
        public studyName?: string,
        public description?: string,
        public startDate?: any,
        public endDate?: any,
        public studyStatus?: StudyStatus,
        public parentProjectId?: Project,
        public device?: Device,
    ) {
    }
}
