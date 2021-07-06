import { Directive, forwardRef, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: '[appRawjsonDirective]',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RawJsonDirective),
      multi: true,
    },
  ],
  // eslint-disable-next-line @angular-eslint/no-host-metadata-property
  host: {
    '(change)': 'onChange($event.target.value)',
    '(input)': 'onChange($event.target.value)',
    '(blur)': 'onTouched()',
  },
})
export class RawJsonDirective implements ControlValueAccessor {
  onChange;
  onTouched;

  constructor(private elementRef: ElementRef) {}

  writeValue(value: any) {
    this.elementRef.nativeElement.value = JSON.stringify(value, null, 2);
  }

  registerOnChange(fn: any) {
    this.onChange = value => {
      fn(this.parseJson(value));
    };
  }

  registerOnTouched(fn: any) {
    this.onTouched = fn;
  }

  private parseJson(value: any) {
    try {
      return JSON.parse(value);
    } catch (e) {
      return null;
    }
  }
}
