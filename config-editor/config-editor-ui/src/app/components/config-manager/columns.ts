import { StoreHeaderGroupComponent } from '@app/components/config-manager/header-groups/store-header-group.component';
import { ReleaseHeaderGroupComponent } from '@app/components/config-manager/header-groups/release-header-group.component';
import { StoreActionCellRendererComponent } from "@app/components/config-manager/cell-renderers/store-action-cell-renderer.component";
import { LabelCellRendererComponent } from '@app/components/config-manager/cell-renderers/label-cell-renderer.component';
import { ReleaseActionsCellRendererComponent } from '@app/components/config-manager/cell-renderers/release-actions-cell-renderer.component';
import { ConfigNameCellRendererComponent } from '@app/components/config-manager/cell-renderers/config-name-cell-renderer.component';

const storeColumns = [
  { 
    field: "config_name",
    minWidth: 150,
    maxWidth: 350,
    headerName: "Config Name",
    cellRenderer: ConfigNameCellRendererComponent,
    // Note: only allow dragging for released configs
    rowDrag: params => params.node.data.releasedVersion > 0,
  },
  { 
    field: "author",
    maxWidth: 125,
  },
  {
    field: "labels",
    minWidth: 250,
    cellRenderer: LabelCellRendererComponent,
    wrapText: true,
  },
  { 
    headerName: "Store Actions",
    minWidth: 200,
    maxWidth: 200,
    cellRenderer: StoreActionCellRendererComponent,
    editable: false,
    colId: "action",
    // Note: searching not on this column
    getQuickFilterText: () => '',
  },
];

const releaseColumns = [
  { 
    headerName: "Release Actions",
    minWidth: 300,
    maxWidth: 350,
    cellRenderer: ReleaseActionsCellRendererComponent,
    editable: false,
    colId: "status",
    // Note: searching not on this column
    getQuickFilterText: () => '',
  },
]

export const configManagerColumns = [
  { 
    headerName: "Store",
    headerGroupComponent: StoreHeaderGroupComponent,
    children: storeColumns,
  },
  {
    headerName: "Release",
    headerGroupComponent: ReleaseHeaderGroupComponent,
    children: releaseColumns,
  },
]