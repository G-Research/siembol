import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCentreComponent } from './test-centre.component';

describe('TestCentreComponent', () => {
  let component: TestCentreComponent;
  let fixture: ComponentFixture<TestCentreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCentreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCentreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
