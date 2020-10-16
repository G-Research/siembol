import { Component, Inject } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { BuildInfo } from "../../model";


@Component({
    selector: 're-about-app-dialog',
    styleUrls: ['build-info-dialog.component.scss'],
    templateUrl: 'build-info-dialog.component.html',
})
export class BuildInfoDialogComponent {
    constructor(private dialogRef: MatDialogRef<BuildInfoDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: BuildInfo){}
}