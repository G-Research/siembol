import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatTableDataSource } from "@angular/material/table";
import { Application } from "@app/model/config-model";
import { EditorService } from "@app/services/editor.service";
import { Observable } from "rxjs";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-application-dialog',
  styleUrls: ['application-dialog.component.scss'],
  templateUrl: 'application-dialog.component.html',
})
export class ApplicationDialogComponent {
  dialogrefAttributes: MatDialogRef<any>;
  applications$: Observable<Application[]>;
  columns = [
    {
      columnDef: 'name',
      header: 'Name',
      cell: (t: Application) => `${t.topology_name}`,
    },
    {
      columnDef: 'id',
      header: 'ID',
      cell: (t: Application) => `${t.topology_id}`,
    },
    {
      columnDef: 'image',
      header: 'Image',
      cell: (t: Application) => `${t.image}`,
    },
  ];
  displayedColumns = ["name", "id", "image", "attributes", "restart"];
  dataSource: MatTableDataSource<Application>;
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: Observable<Application[]>,
    private service: EditorService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {
    this.applications$ = data;
    this.applications$.subscribe(t => {
      this.dataSource = new MatTableDataSource(t);
      this.cd.markForCheck();
    })
  }

  onClickClose() {
    this.dialogref.close();
  }

  onRestartApplication(applicationName: string) {
    this.applications$ = this.service.configLoader.restartApplication(applicationName);
    this.applications$.subscribe(t => {
      const filter = this.dataSource.filter;
      this.dataSource = new MatTableDataSource(t);
      this.dataSource.filter = filter;
      this.cd.markForCheck();
    })
  }

  onViewAttributes(attributes: string, templateRef: TemplateRef<any>) {
    this.dialogrefAttributes = this.dialog.open(
      templateRef, 
      { 
        data: JSON.parse(atob(attributes)),
      });
  }

  onClickCloseAttributes() {
    this.dialogrefAttributes.close();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}