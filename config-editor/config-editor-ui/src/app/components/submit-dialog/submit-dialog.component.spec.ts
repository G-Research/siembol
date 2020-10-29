import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of } from 'rxjs';
import { SubmitDialogComponent } from './submit-dialog.component';

const testResponseOk = {}

const dialogRefMock = {
  close: () => {}
}

const dataMock = {
  validate: () => of(undefined),
  submit: () => of(false)
}

describe('SubmitDialogComponent', () => {
  let component: SubmitDialogComponent;
  let fixture: ComponentFixture<SubmitDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ SubmitDialogComponent ],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: MAT_DIALOG_DATA, useValue: dataMock }
      ]
    })
      .compileComponents();
    fixture = TestBed.createComponent(SubmitDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should close', () => {
    const spy = spyOn(component.dialogref, 'close');
    component.onClickClose();
    expect(spy).toHaveBeenCalled();
  })

  it('should be valid then submitted', () => {
    expect(component.isValid).toBeFalse();
    spyOn(component, 'validate').and.returnValue(of(testResponseOk));
    component.ngOnInit();
    expect(component.isValid).toBeTrue();

    const spy = spyOn(component.dialogref, 'close');
    spyOn(component, 'submit').and.returnValue(of(true));
    component.onClickSubmit();
    expect(spy).toHaveBeenCalledWith(true);
  });

});
