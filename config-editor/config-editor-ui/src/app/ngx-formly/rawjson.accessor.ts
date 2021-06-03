import { Directive, forwardRef, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: '[rawJsonAccessor]',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RawJsonAccessor),
      multi: true,
    },
  ],
  host: {
    '(change)': 'onChange($event.target.value)',
    '(input)': 'onChange($event.target.value)',
    '(blur)': 'onTouched()',
  },
})
export class RawJsonAccessor implements ControlValueAccessor {
  onChange;
  onTouched;

  constructor(private elementRef: ElementRef) {}

  writeValue(value: any) {
    this.elementRef.nativeElement.value = JSON.stringify(value, null, 2);
  }

  registerOnChange(fn: any) {
    this.onChange = value => {
      fn(this.checkJson(value));
    };
  }

  registerOnTouched(fn: any) {
    this.onTouched = fn;
  }

  checkJson(value: any) {
    try {
      return JSON.parse(value);
    } catch (e) {
      return null;
    }
  }
}
