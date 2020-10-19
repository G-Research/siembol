import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { StatusCode } from '@app/commons/status-code';
import { of, throwError } from 'rxjs';
import { SubmitDialogComponent } from './submit-dialog.component';

const testResponseOk = {
  status_code: StatusCode.OK,
  attributes: {}
}

const testResponseError = {
  status_code: StatusCode.ERROR,
  attributes: { message: 'Error'}
}

const testResponseBadRequest = {
  status_code: StatusCode.BAD_REQUEST,
  attributes: { message: 'Error'}
}

const testResponseBadRequest2 = {
  status_code: StatusCode.BAD_REQUEST,
  attributes: {}
}

const dialogRefMock = {
  close: () => {}
}

const dataMock = {
  validate: () => of(undefined),
  submit: () => of(true)
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
    component.configValidity$ = of(testResponseOk)
    component.ngOnInit();
    expect(component.isValid).toBeTrue();

    const spy = spyOn(component.dialogref, 'close');
    component.onClickSubmit();
    fixture.detectChanges();
    expect(spy).toHaveBeenCalledWith(true);
  });

  it('should not validate error in response', () => {
    component.configValidity$ = of(testResponseError)
    component.ngOnInit();
    expect(component.isValid).toBeFalse();
    expect(component.message).toBe('Error');
  });

  it('should not validate bad_request in response', () => {
    component.configValidity$ = of(testResponseBadRequest)
    component.ngOnInit();
    expect(component.isValid).toBeFalse();
    expect(component.message).toBe('Error');
  });

  it('should not validate bad_request in response + no message', () => {
    component.configValidity$ = of(testResponseBadRequest2)
    component.ngOnInit();
    expect(component.isValid).toBeFalse();
  });

  it('should be valid then not submit', () => {
    const exceptionInfo = {
      message: 'Error',
      status: StatusCode.ERROR
    }
    component.configValidity$ = of(testResponseOk)
    component.ngOnInit();

    spyOn(component, 'submit').and.returnValue(throwError(exceptionInfo));
    component.onClickSubmit();
    expect(component.failedSubmit).toBe(true);
    expect(component.message).toBe('Server error, please contact administrator.\nError');

    fixture.detectChanges();
    const error = fixture.debugElement.nativeElement.innerHTML;
    expect(error).toContain('Submitting failed');

  });
});