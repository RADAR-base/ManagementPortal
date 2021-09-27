import { NgModule, Pipe } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Pipe({name: 'translate'})
class MockTranslatePipe {
    transform(key: any) {
        return key;
    }
}

@NgModule({
    declarations: [MockTranslatePipe],
    providers: [
        DatePipe,
    ],
    imports: [
        HttpClientTestingModule,
    ],
    exports: [FormsModule, MockTranslatePipe],
})
export class ManagementPortalTestModule {}
