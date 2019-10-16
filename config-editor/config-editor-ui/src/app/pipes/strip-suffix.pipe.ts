import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'stripSuffix' })
export class StripSuffixPipe implements PipeTransform {
  public transform(text: string, remove: string): string {
    if (text === undefined) {
      return '';
    }
    if (text.endsWith(remove)) {
      return text.substring(0, text.length - remove.length);
    }

    return text;
  }
}
