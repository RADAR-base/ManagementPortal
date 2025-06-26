import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YOURCOMPONENTNAMEComponent } from './yourcomponentname.component';

describe('YOURCOMPONENTNAMEComponent', () => {
  let component: YOURCOMPONENTNAMEComponent;
  let fixture: ComponentFixture<YOURCOMPONENTNAMEComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ YOURCOMPONENTNAMEComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(YOURCOMPONENTNAMEComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
