import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-page-not-found',
  styleUrls: ['./page-not-found.component.scss'],
  template: '<div><mat-card><mat-card-content>Page not found!</mat-card-content></mat-card></div>',
})
export class PageNotFoundComponent { }
