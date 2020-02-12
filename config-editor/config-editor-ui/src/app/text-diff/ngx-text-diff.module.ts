import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgScrollbarModule } from 'ngx-scrollbar';
import { FormatLinePipe } from './format-line.pipe';
import { LoaderSpinnerComponent } from './loader-spinner/loader-spinner.component';
import { ContainerDirective } from './ngx-text-diff-container.directive';
import { NgxTextDiffComponent } from './ngx-text-diff.component';

@NgModule({
  imports: [CommonModule, FormsModule, ScrollingModule, NgScrollbarModule],
  declarations: [NgxTextDiffComponent, LoaderSpinnerComponent, FormatLinePipe, ContainerDirective],
  exports: [NgxTextDiffComponent],
})
export class NgxTextDiffModule {}
