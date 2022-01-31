import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatTableDataSource } from "@angular/material/table";
import { Application, applicationManagerColumns, displayedApplicationManagerColumns } from "@app/model/config-model";
import { AppService } from "@app/services/app.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-application-dialog',
  styleUrls: ['application-dialog.component.scss'],
  templateUrl: 'application-dialog.component.html',
  
})
export class ApplicationDialogComponent {
  MAX_DIALOG_WIDTH = '800px';
  dialogrefInfo: MatDialogRef<any>;
  dataSource: MatTableDataSource<Application>;
  columns = applicationManagerColumns;
  displayedColumns = displayedApplicationManagerColumns;
  restartedApplications: string[] = [];
  disableAllRestart = false;
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    private service: AppService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: Application[]
  ) {
      this.createTable(data);
  }

  onClickClose() {
    this.dialogref.close();
  }

  onRestartApplication(serviceName: string, applicationName: string, templateRef: TemplateRef<any>) {
    this.service.restartApplication(serviceName, applicationName).subscribe(a => {
      this.createTable(a);
      this.restartedApplications.push(applicationName);
    });
    this.openInfoDialog(applicationName, templateRef);  
  }

  onViewAttributes(attributes: string[], templateRef: TemplateRef<any>) {
    this.openInfoDialog(attributes.map(a => JSON.parse(atob(a))), templateRef);
  }

  onClickCloseInfo() {
    this.dialogrefInfo.close();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  restartAllApplications(templateRef: TemplateRef<any>) {
    this.service.restartAllApplications().subscribe((apps: Application[]) => {
      this.createTable(apps);
      this.onClickCloseInfo();
      this.disableAllRestart = true;
      this.cd.markForCheck();
      this.openInfoDialog("all applications", templateRef);
    })
  }
  
  openInfoDialog(data: any, templateRef: TemplateRef<any>) {
    this.dialogrefInfo = this.dialog.open(
      templateRef, 
      { 
        data,
        maxWidth: this.MAX_DIALOG_WIDTH,
      });
  }

  private createTable(a: Application[]) {
    const filter = this.dataSource?.filter;
    this.dataSource = new MatTableDataSource(a);
    this.dataSource.filter = filter;
    this.cd.markForCheck();
  }
}