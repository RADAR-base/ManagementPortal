// Extracted from JHipster PaginationUtil

export function parseAscending(sort: string): boolean {
    let sortArray = sort.split(',');
    sortArray = sortArray.length > 1 ? sortArray : sort.split('%2C');
    if (sortArray.length > 1) {
        return sortArray.slice(-1)[0] === 'asc';
    }
    // default to true if no sort is defined
    return true;
}

export function parsePage(page: string): number {
    return parseInt(page, 10);
}

export function parsePredicate(sort: string): string {
    return sort.split(',')[0].split('%2C')[0];
}
