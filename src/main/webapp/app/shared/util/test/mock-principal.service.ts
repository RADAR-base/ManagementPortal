
export class MockPrincipal {

    identity: any;
    fakeResponse: any;

    constructor() {
        this.fakeResponse = {};
        this.identity = jasmine.createSpy('identity').and.returnValue(Promise.resolve(this.fakeResponse));
    }

    setResponse(json: any): void {
        this.fakeResponse = json;
    }
}
