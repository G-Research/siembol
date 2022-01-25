import { ChangeDetectionStrategy, ChangeDetectorRef, Component, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
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
  disableRestart = false;
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    private service: AppService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {
    this.service.getAllApplications().subscribe(a => {
      this.createTable(a);
    })
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

  openConfirmRestartAllApplications(templateRef: TemplateRef<any>) { 
    this.service.getAllApplications().subscribe(applications => {
      this.openInfoDialog(applications.map(app => app.topology_name), templateRef);
    })
  }

  restartAllApplications(templateRef: TemplateRef<any>) {
    this.service.restartAllApplications().subscribe((apps: Application[]) => {
      this.createTable(apps);
      this.onClickCloseInfo();
      this.disableRestart = true;
      this.cd.markForCheck();
      this.openInfoDialog("all applications", templateRef);
    })
  }

  private createTable(a: Application[]) {
    const filter = this.dataSource?.filter;
    this.dataSource = new MatTableDataSource(a);
    this.dataSource.filter = filter;
    this.cd.markForCheck();
  }

  private openInfoDialog(data: any, templateRef: TemplateRef<any>) {
    this.dialogrefInfo = this.dialog.open(
      templateRef, 
      { 
        data,
        maxWidth: this.MAX_DIALOG_WIDTH,
      });
  }
}