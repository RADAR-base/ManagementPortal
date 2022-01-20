export class MockAccountService {

    get: any;
    save: any;
    fakeResponse: any;

    constructor() {
        this.fakeResponse = null;
        this.get = jasmine.createSpy('get').and.returnValue(this);
        this.save = jasmine.createSpy('save').and.returnValue(this);
    }

    subscribe(callback: any) {
        callback(this.fakeResponse);
    }

    setResponse(json: any): void {
        this.fakeResponse = json;
    }
}
