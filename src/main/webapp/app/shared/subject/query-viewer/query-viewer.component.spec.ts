import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryViewerComponent } from './query-viewer.component';

describe('QueryViewerComponent', () => {
  let component: QueryViewerComponent;
  let fixture: ComponentFixture<QueryViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QueryViewerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QueryViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
