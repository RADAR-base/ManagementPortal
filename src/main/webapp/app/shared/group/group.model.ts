export class Group {
    public id?: number;
    public name: string;
    public projectId?: number;
    public projectName?: string;
}

export function copyGroup(group: Group): Group {
    return {...group};
}
