import { Directive, ElementRef, Input } from '@angular/core';

@Directive({
  // tslint:disable-next-line:directive-selector
  selector: '[tdContainer]',
})
export class ContainerDirective {
  @Input() id: string;

  element: HTMLTableHeaderCellElement;

  constructor(_el: ElementRef) {
    this.element = _el.nativeElement;
  }
}
