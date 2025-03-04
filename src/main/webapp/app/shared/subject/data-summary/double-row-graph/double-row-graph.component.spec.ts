import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoubleRowGraphComponent } from './double-row-graph.component';

describe('DoubleRowGraphComponent', () => {
  let component: DoubleRowGraphComponent;
  let fixture: ComponentFixture<DoubleRowGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DoubleRowGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DoubleRowGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
