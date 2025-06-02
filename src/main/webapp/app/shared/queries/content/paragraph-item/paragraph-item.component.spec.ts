import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParagraphItemComponent } from './paragraph-item.component';

describe('ParagraphItemComponent', () => {
  let component: ParagraphItemComponent;
  let fixture: ComponentFixture<ParagraphItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ParagraphItemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ParagraphItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
