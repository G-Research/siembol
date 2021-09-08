import { ChangeDetectionStrategy, Component, Inject, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatTableDataSource } from "@angular/material/table";
import { Topology } from "@app/model/config-model";
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
  topologies$: Observable<Topology[]>;
  columns = [
    {
      columnDef: 'name',
      header: 'Name',
      cell: (t: Topology) => `${t.topology_name}`,
    },
    {
      columnDef: 'id',
      header: 'ID',
      cell: (t: Topology) => `${t.topology_id}`,
    },
    {
      columnDef: 'image',
      header: 'Image',
      cell: (t: Topology) => `${t.image}`,
    },
  ];
  displayedColumns = ["name", "id", "image", "attributes", "restart"];
  dataSource: MatTableDataSource<Topology>;
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: Observable<Topology[]>,
    private service: EditorService,
    private dialog: MatDialog
  ) {
    this.topologies$ = data;
    this.topologies$.subscribe(t => {
      this.dataSource = new MatTableDataSource(t);
    })
  }

  onClickClose() {
    this.dialogref.close();
  }

  onRestartTopology(topologyName: string) {
    this.topologies$ = this.service.configLoader.restartTopology(topologyName);
    this.topologies$.subscribe(t => {
      this.dataSource = new MatTableDataSource(t);
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