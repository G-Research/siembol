import { Component } from '@angular/core';
import { ChangeDetectionStrategy } from '@angular/core';
import { Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfigData, Config } from '@app/model';

export interface DiffContent {
    leftContent: string;
    rightContent: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-json-viewer',
  styleUrls: ['./json-viewer.component.scss'],
  templateUrl: './json-viewer.component.html',
})
export class JsonViewerComponent {
  public leftContent: ConfigData;
  public rightContent: ConfigData;

  constructor(public dialogRef: MatDialogRef<JsonViewerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {

    this.leftContent = data.config1;
    this.rightContent = data.config2;
  }

  onCompareResults($event) {
      // noop
  }
}
