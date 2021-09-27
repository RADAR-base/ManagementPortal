// Based on JHipster DateUtils

export function convertDateTimeFromServer(date: any): Date | null {
    if (date) {
        return new Date(date);
    } else {
        return null;
    }
}

export function toDate(date: any): Date | null {
    if (date === undefined || date === null) {
        return null;
    }
    let dateParts = date.split(/\D+/);
    return new Date(dateParts[0], dateParts[1] - 1, dateParts[2], dateParts[3], dateParts[4]);
}
