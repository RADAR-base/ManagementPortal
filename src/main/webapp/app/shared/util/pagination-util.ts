// Extracted from JHipster PaginationUtil

export function parseSort(sort: string): { predicate: string, ascending: boolean } {
    let sortArray = sort.split(',');
    if (sortArray.length === 1) {
        sortArray = sort.split('%2C');
    }
    return {
        predicate: sortArray[0],
        ascending: sortArray.length === 1 || sortArray.slice(-1)[0] === 'asc',
    };
}

export function parsePage(page: string): number {
    return parseInt(page, 10);
}
