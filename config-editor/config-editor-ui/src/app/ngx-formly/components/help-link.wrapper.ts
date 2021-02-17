import { Component, ViewChild, ViewContainerRef, TemplateRef, AfterViewInit } from '@angular/core';
import { FieldWrapper } from '@ngx-formly/core';
import { copyTextToClipboard } from '@app/commons';
import { PopupService } from '@app/popup.service';


@Component({
  selector: 'formly-help-link-wrapper',
  template: `
    <ng-container #fieldComponent></ng-container>
    <ng-template #matSuffix>
      <button mat-icon-button matToolTipClass="link-tooltip" [matTooltip]="to.link" (click)="onClick()" (contextmenu)="onRightClick()">
          <mat-icon>{{ suffixIcon }}</mat-icon>
        </button>
    </ng-template>
  `,
    styles: [`
        ::ng-deep .link-tooltip {
            max-width: 100px !important;
        }
    `]
})
export class HelpLinkWrapperComponent extends FieldWrapper implements AfterViewInit {
  suffixIcon: string;
  @ViewChild('fieldComponent', { read: ViewContainerRef }) fieldComponent: ViewContainerRef;
  @ViewChild('matSuffix') matSuffix: TemplateRef<any>;

    constructor(private snackbar: PopupService) {
      super();
    }
    
  ngAfterViewInit() {
    if (this.matSuffix ) {
      setTimeout(() => this.to.suffix = this.matSuffix);
    }
    if (!this.to.suffixIcon) {
      this.suffixIcon = "help_outline";
    } else {
      this.suffixIcon = this.to.suffixIcon;
    }
  }
    
  onClick() {
    window.open(this.to.link, "_blank");
  }

  onRightClick() {
    this.snackbar.openNotification(
    copyTextToClipboard(this.to.link)
      ? 'URL copied to clipboard!'
      : 'Error copying to clipboard'
    );
    return false;
  }
}
