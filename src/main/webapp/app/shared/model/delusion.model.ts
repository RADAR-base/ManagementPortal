export interface Delusion {
    key?: string;
    label?: string;
}

export function copyDelusion(delusion: Delusion): Delusion {
    return {...delusion};
}
