import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({ name: 'highlightVariables' })
export class HighlightVariablesPipe implements PipeTransform {

  constructor(private sanitiser: DomSanitizer) { }

  transform(text: string): SafeHtml {
    if (!text) {
      return '';
    }

    const highlightedText = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;')
      .replace(/\n/g, '<br/>')
      .replace(/\${([\w.:]+)}/g, '<span style="color: #ffb10a">\${<span style="color: #a4eaf9">$1</span>}</span>');

    return this.sanitiser.bypassSecurityTrustHtml(highlightedText);
  }
}
