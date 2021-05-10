import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ConfigData, Config } from '../../model/config-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-config-tile',
  styleUrls: ['./config-tile.component.scss'],
  templateUrl: './config-tile.component.html',
})
export class ConfigTileComponent {
  @Input() config: Config;
  @Input() hideAddDeployment: boolean;

  @Output() onEdit = new EventEmitter<number>();
  @Output() onView = new EventEmitter<number>();
  @Output() onAddToDeployment = new EventEmitter<number>();
  @Output() onClone = new EventEmitter<number>();
  @Output() onDeleteConfigFromStore = new EventEmitter<number>();

  constructor() {}

  editConfig() {
    this.onEdit.emit();
  }

  viewConfig() {
    this.onView.emit();
  }

  addToDeployment() {
    this.onAddToDeployment.emit();
  }

  cloneConfig() {
    this.onClone.emit();
  }

  deleteConfigFromStore() {
    this.onDeleteConfigFromStore.emit();
  }
}
