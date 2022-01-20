export interface Group {
    id?: number;
    name?: string;
    projectId?: number;
    projectName?: string;
}

export function copyGroup(group: Group): Group {
    return {...group};
}
