import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

export class MockActivatedRoute extends ActivatedRoute {

    constructor(parameters?: any) {
        super();
        this.queryParams = of(parameters);
        this.params = of(parameters);
    }
}

export class MockRouter {
    navigate = jasmine.createSpy('navigate');
}
