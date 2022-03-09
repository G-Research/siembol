import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Config } from '../../model/config-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-config-tile',
  styleUrls: ['./config-tile.component.scss'],
  templateUrl: './config-tile.component.html',
})
export class ConfigTileComponent {
  @Input() config: Config;
  @Input() notDeployed: boolean;

  @Output() readonly edit = new EventEmitter<number>();
  @Output() readonly view = new EventEmitter<number>();
  @Output() readonly addToRelease = new EventEmitter<number>();
  @Output() readonly clone = new EventEmitter<number>();
  @Output() readonly deleteFromStore = new EventEmitter<number>();

  editConfig() {
    this.edit.emit();
  }

  viewConfig() {
    this.view.emit();
  }

  addConfigToRelease() {
    this.addToRelease.emit();
  }

  cloneConfig() {
    this.clone.emit();
  }

  deleteConfigFromStore() {
    this.deleteFromStore.emit();
  }
}
