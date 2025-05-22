import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RadarDataComponent } from './radar-data.component';

describe('RadarDataComponent', () => {
  let component: RadarDataComponent;
  let fixture: ComponentFixture<RadarDataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RadarDataComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RadarDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
