import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SingleRowGraphComponent } from './single-row-graph.component';

describe('SingleRowGraphComponent', () => {
  let component: SingleRowGraphComponent;
  let fixture: ComponentFixture<SingleRowGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SingleRowGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SingleRowGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
