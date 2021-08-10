import { ChangeDetectionStrategy, Component, Inject } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { EditorService } from "@app/services/editor.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-application-dialog',
  styleUrls: ['application-dialog.component.scss'],
  templateUrl: 'application-dialog.component.html',
})
export class ApplicationDialogComponent {
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: ApplicationDialogComponent,
    private service: EditorService
  ) {}

  onClickClose() {
    this.dialogref.close();
  }

  getTopologies() {

  }
}