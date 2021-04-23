import { Component, ViewChild, ViewContainerRef, TemplateRef, AfterViewInit } from '@angular/core';
import { FieldWrapper } from '@ngx-formly/core';
import { copyTextToClipboard } from '@app/commons';
import { PopupService } from '@app/services/popup.service';

@Component({
  selector: 'formly-help-link-wrapper',
  template: `
    <ng-container #fieldComponent></ng-container>
    <ng-template #matSuffix>
      <button
        *ngIf="to.showHelpLink != false"
        mat-icon-button
        matToolTipClass="link-tooltip"
        [matTooltip]="to.link"
        (click)="onClick()"
        (contextmenu)="onRightClick()"
      >
        <mat-icon>{{ suffixIcon }}</mat-icon>
      </button>
    </ng-template>
  `,
  styles: [
    `
      ::ng-deep .link-tooltip {
        max-width: 100px !important;
      }
    `,
  ],
})
export class HelpLinkWrapperComponent extends FieldWrapper implements AfterViewInit {
  @ViewChild('fieldComponent', { read: ViewContainerRef }) fieldComponent: ViewContainerRef;
  @ViewChild('matSuffix') matSuffix: TemplateRef<any>;
  suffixIcon: string;

  constructor(private snackbar: PopupService) {
    super();
  }

  ngAfterViewInit() {
    if (this.matSuffix) {
      this.to.suffix = this.matSuffix;
    }
    this.suffixIcon = this.to.suffixIcon ? this.to.suffixIcon : 'help_outline';
  }

  onClick() {
    window.open(this.to.link, '_blank');
  }

  onRightClick() {
    this.snackbar.openNotification(
      copyTextToClipboard(this.to.link) ? 'URL copied to clipboard!' : 'Error copying to clipboard'
    );
    return false;
  }
}
