import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatTableDataSource } from "@angular/material/table";
import { Application, applicationManagerColumns, displayedApplicationManagerColumns } from "@app/model/config-model";
import { EditorService } from "@app/services/editor.service";
import { Observable } from "rxjs";
import { shareReplay } from "rxjs/operators";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-application-dialog',
  styleUrls: ['application-dialog.component.scss'],
  templateUrl: 'application-dialog.component.html',
})
export class ApplicationDialogComponent {
  dialogrefAttributes: MatDialogRef<any>;
  applications$: Observable<Application[]>;
  dataSource: MatTableDataSource<Application>;
  columns = applicationManagerColumns;
  displayedColumns = displayedApplicationManagerColumns;
  restartedApplications: string[] = [];
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: Observable<Application[]>,
    private service: EditorService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {
    this.applications$ = data;
    this.applications$.subscribe(a => {
      this.createTable(a);
    })
  }

  onClickClose() {
    this.dialogref.close();
  }

  onRestartApplication(applicationName: string, templateRef: TemplateRef<any>) {
    this.applications$ = this.service.configLoader.restartApplication(applicationName).pipe(shareReplay());
    this.restartedApplications.push(applicationName);
    this.applications$.subscribe(a => {
      this.createTable(a);
    })
    this.dialogrefAttributes = this.dialog.open(
      templateRef, 
      { 
        data: applicationName,
        maxWidth: '800px',
      });
    
  }

  onViewAttributes(attributes: string[], templateRef: TemplateRef<any>) {
    this.dialogrefAttributes = this.dialog.open(
      templateRef, 
      { 
        data: attributes.map(a => JSON.parse(atob(a))), 
      });
  }

  onClickCloseAttributes() {
    this.dialogrefAttributes.close();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  private createTable(a: Application[]) {
    const filter = this.dataSource?.filter;
    this.dataSource = new MatTableDataSource(a);
    this.dataSource.filter = filter;
    this.cd.markForCheck();
  }
}